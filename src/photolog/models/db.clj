(ns photolog.models.db
  (:require [clojure.java.jdbc.deprecated :as sql]
            [noir.util.crypt :as crypt]
            [environ.core :refer [env]]))
;TODO's:
; use macro for with-connection db
; update to not use deprecated 



(def db {:subprotocol (env :proto) 
         :subname (env :subname)
         :user (env :db-user)
         :password (env :password) })

(def album-status 
  {:draft 0 :published 1})

(defn drop-tables []
  (sql/with-connection db
    (doseq [tbl ["photos" "albums" "users"]]
      (sql/do-commands
        (str "DROP TABLE IF EXISTS " tbl)))))

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

(defn get-albums [& {:keys [per_page page status] :or {per_page 10 page 1 status :published}}]
  (let [per_page (if (nil? per_page) 10 (Integer. per_page)) page (if (nil? page) 1 (Integer. page))]
    (sql/with-connection db
      (sql/with-query-results
        res ["SELECT * FROM albums WHERE status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?" 
             (status album-status) per_page (* per_page (- page 1))]
        (doall res)))))

(defn get-photos [& {:keys [per_page page] :or {per_page 10 page 1}}]
  (let [per_page (if (nil? per_page) 10 (Integer. per_page)) page (if (nil? page) 1 (Integer. page))]
    ; might need a join to get a user name here bro
    (sql/with-connection db
      (sql/with-query-results
        res ["SELECT * FROM photos ORDER BY created_at DESC LIMIT ? OFFSET ?" per_page (* per_page (- page 1))]
        (doall res)))))


(defn insert-album [{:keys [name description status]} {:keys [id]}]
  (let [stat (or status 0)]
  (sql/with-connection db
    (sql/insert-record
      :albums
      {:name name :description description :user_id id :status (Integer. stat)}))))

(defn insert-photo-into-album [{:keys [name description filename]} albumid]
  (sql/with-connection db
    (sql/insert-record
      :photos
      {:name name :description description :filename filename :album_id albumid})))


; mite b unused
(defn insert-photo [name filename description userid]
  (sql/with-connection db
    (sql/insert-record
      :photos
      {:name name :filename filename :description description :userid userid})))

; move to utils?
(defn current-time []
  (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defn update-album [{:keys [name description id status]} {user_id :id}]
  (let [stat (or status 0)]
  (sql/with-connection db
    (let [now (current-time)]
      (sql/update-values :albums ["id = ?" (Integer. id)] {:name name :description description :user_id (Integer. user_id) :updated_at now :status (Integer. stat)})))))
    

(defn get-album [album-id]
  (sql/with-connection db
    (sql/with-query-results
      res ["SELECT * FROM albums where id = ?" (Integer. album-id)] (first res))))

(defn get-album-with-photos [album-id]
  (sql/with-connection db
    (sql/with-query-results
      res ["SELECT * FROM albums LEFT OUTER JOIN photos ON albums.id = photos.album_id WHERE albums.id = ?" (Integer. album-id)] (doall res))))

; extract IN-clause logic into own fn
(defn delete-albums [ids]
  (sql/with-connection db
    (sql/delete-rows :albums (flatten [(str "id IN (" (clojure.string/join ", " (take (count ids) (repeat "?"))) ")") ids]))))

(defn delete-photos [ids]
  (sql/with-connection db
    (sql/delete-rows :photos (flatten [(str "id IN (" (clojure.string/join ", " (take (count ids) (repeat "?"))) ")") ids]))))

(defn get-user [username]
  (sql/with-connection db
    (sql/with-query-results
      res ["SELECT * FROM users WHERE username = ?" username]
      (first res))))

(defn insert-user [username password]
  (sql/with-connection db
    (sql/insert-record
      :users {:username username :password (crypt/encrypt password)})))

(defn create-test-user []
  (insert-user "matt" "123123"))
