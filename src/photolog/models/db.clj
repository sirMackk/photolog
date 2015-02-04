(ns photolog.models.db
  (:require [clojure.java.jdbc.deprecated :as sql]
            [noir.util.crypt :as crypt]
            [environ.core :refer [env]]))
;TODO's:
; some db functions cast to Integer, some only accept integer. Unify this.
; update to not use deprecated 
; macro out table create with-connection db

(def db {:subprotocol (env :proto) 
         :subname (env :subname)
         :user (env :db-user)
         :password (env :password) })

(defmacro with-db [f & body]
  `(sql/with-connection db (~f ~@body)))

(def album-status 
  {:draft 0 :published 1})

(def album-count-admin
  (try
    (atom (sql/with-connection db
            (sql/with-query-results res
              ["SELECT COUNT(id) FROM albums"]
              (:count (first res)))))
    (catch Exception ex
      (atom 0))))

; refactor these two atoms out
; check for the right exception
(def album-count-guest
  (try
    (atom (sql/with-connection db
            (sql/with-query-results res
              ["SELECT COUNT(id) FROM albums WHERE status IN (0, 1)"]
              (:count (first res)))))
    (catch Exception ex
      (atom 0))))

(defn generate-in [args]
  (str "IN (" (clojure.string/join ", " (take (count args) (repeat "?"))) ")"))

(defn drop-tables []
  (with-db doseq [tbl ["photos" "albums" "users"]]
    (sql/do-commands
      (str "DROP TABLE IF EXISTS " tbl))))

(defn create-table-albums []
  (sql/with-connection db
    (sql/create-table
      :albums
      [:id "SERIAL PRIMARY KEY"]
      [:created_at "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP"]
      [:updated_at "TIMESTAMPTZ DEFAULT NULL"]
      [:name "VARCHAR(128) DEFAULT 'Untitled'"]
      [:description "VARCHAR(512) DEFAULT NULL"]
      [:status "SMALLINT DEFAULT 0"]
      [:user_id "INTEGER REFERENCES users (id) ON UPDATE CASCADE ON DELETE SET NULL"])
    (sql/do-commands "CREATE INDEX idx_albums_create_at ON albums (created_at)"
                     "CREATE INDEX idx_albums_name ON albums (name)")))
      

(defn create-table-photos []
  (sql/with-connection db
    (sql/create-table
      :photos
      [:id "SERIAL PRIMARY KEY"]
      [:created_at "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP"]
      [:updated_at "TIMESTAMPTZ DEFAULT NULL"]
      [:name "VARCHAR(32) DEFAULT 'Untitled'"]
      [:filename "VARCHAR(255)"]
      [:description "TEXT DEFAULT NULL"]
      [:album_id "INTEGER REFERENCES albums (id) on UPDATE CASCADE ON DELETE CASCADE NOT NULL"])
    (sql/do-commands "CREATE INDEX idx_photos_created_at ON photos (created_at)"
                     "CREATE INDEX idx_photos_album_id ON photos (album_id)"
                     "CREATE INDEX idx_photos_name ON photos (name)")))

(defn create-table-users []
  (sql/with-connection db
    (sql/create-table
      :users
      [:id "SERIAL PRIMARY KEY"]
      [:username "VARCHAR(64) NOT NULL"]
      [:password "VARCHAR(255) NOT NULL"]
      [:created_at "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP"]
      [:updated_at "TIMESTAMPTZ DEFAULT NULL"])
    (sql/do-commands "CREATE INDEX idx_users_username ON users (username)")))

(defn get-albums [& {:keys [per_page page status] :or {per_page 10 page 1 status [:published :draft]}}]
  (let [per_page 
        (if (nil? per_page) 10 (Integer. per_page)) 
        page 
        (if (nil? page) 1 (Integer. page))]
    (with-db sql/with-query-results
        res (vec (flatten [(str "SELECT * FROM albums WHERE status " (generate-in status) " ORDER BY created_at DESC LIMIT ? OFFSET ?")
             (map album-status status) per_page (* per_page (- page 1))]))
        (doall res))))

(defn get-photos [& {:keys [per_page page] :or {per_page 10 page 1}}]
  (let [per_page (if (nil? per_page) 10 (Integer. per_page)) page (if (nil? page) 1 (Integer. page))]
    ; might need a join to get a user name here bro
    (with-db sql/with-query-results
        res ["SELECT * FROM photos ORDER BY created_at DESC LIMIT ? OFFSET ?" per_page (* per_page (- page 1))]
        (doall res))))

(defn get-albums-with-photos [& {:keys [per_page page status] :or {per_page 5 page 1 status [:published]}}]
  (let [per_page ; this has to be refactorable
        (if (nil? per_page) 10 (Integer. per_page))
        page
        (if (nil? page) 1 (Integer. page))]
    (prn page)
    (with-db sql/with-query-results
      res (vec (flatten [(str "SELECT * FROM albums LEFT JOIN photos ON albums.id = photos.album_id WHERE albums.status " (generate-in status) " ORDER BY albums.created_at DESC LIMIT ? OFFSET ?")
                         (map album-status status) per_page (* per_page (- page 1))]))
      (doall res)))) ; add pagination in controller or factor out paginatin to db totally

(defn insert-album [{:keys [name description status]} {:keys [id]}]
  (let [stat (or status 0) new-record
    (with-db sql/insert-record
      :albums
      {:name name :description description :user_id id :status (Integer. stat)})]
    (do
      (swap! album-count-admin inc)
      (swap! album-count-guest inc))
    new-record
    ))

(defn insert-photo-into-album [{:keys [name description filename]} albumid]
  (with-db sql/insert-record
      :photos
      {:name name :description description :filename filename :album_id albumid}))


; mite b unused
(defn insert-photo [name filename description userid]
  (with-db sql/insert-record
      :photos
      {:name name :filename filename :description description :userid userid}))

; move to utils?
(defn current-time []
  (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defn update-album [{:keys [name description id status]} {user_id :id}]
  (let [stat (or status 0)]
    (with-db let [now (current-time)]
      (sql/update-values :albums ["id = ?" (Integer. id)] {:name name :description description :user_id (Integer. user_id) :updated_at now :status (Integer. stat)}))))

(defn update-photo [{:keys [name desc id]}]
  (with-db let [now (current-time)]
    (sql/update-values :photos ["id = ?" (Integer. id)]
                       {:name name :description desc :updated_at now})))

(defn get-album [album-id]
  (with-db sql/with-query-results
      res ["SELECT * FROM albums where id = ?" (Integer. album-id)] (first res)))

(defn get-album-with-photos [album-id]
  (with-db sql/with-query-results
      res ["SELECT * FROM albums LEFT OUTER JOIN photos ON albums.id = photos.album_id WHERE albums.id = ?" (Integer. album-id)] (doall res)))

(defn in-clause [args]
  (flatten [(str "id IN (" (clojure.string/join ", " (take (count args) (repeat "?"))) ")") args]))

(defn delete-albums [ids]
  (with-db sql/delete-rows :albums (in-clause ids)))

(defn delete-photos [ids]
  (with-db sql/delete-rows :photos (in-clause ids)))

(defn get-user [username]
  (with-db sql/with-query-results
      res ["SELECT * FROM users WHERE username = ?" username]
      (first res)))

(defn insert-user [username password]
  (with-db sql/insert-record
      :users {:username username :password (crypt/encrypt password)}))

(defn create-test-user []
  (insert-user "matt" "123123"))
