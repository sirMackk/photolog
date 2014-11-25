(ns photolog.models.db
  (:require [clojure.java.jdbc :as sql]
            [noir.util.crypt :as crypt]))


(def db {:subprotocol "postgresl"
         :subname "//localhost/photolog_dev"
         :user "developer"
         :password "a" })

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
      [:user_id "INTEGER REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE NOT NULL"])
    (sql/do-commands "CREATE INDEX idx_photos_created_at ON photos (created_at)"
                     "CREATE INDEX idx_photos_user_id ON photos (user_id)"
                     "CREATE INDEX idx_photos_name ON photos (name)")))

(defn create-table-users []
  (sql/with-connection db
    (sql/create-table 
      :users
      [:id "SERIAL PRIMAR KEY"]
      [:username "VARCHAR(64) NOT NULL"]
      [:password "VARCHAR(255) NOT NULL"]
      [:created_at "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP"]
      [:updated_at "TIMESTAMPTZ DEFAULT NULL"])
    (sql/do-commands "CREATE INDEX idx_users_username ON (users)")))

(defn get-photos [& {:keys [per_page page] :or {per_page 10 page 1}}]
  ; might need a join to get a user name here bro
  (sql/with-connection db
    (sql/with-query-results
      res ["SELECT * FROM photos ORDER BY created_at DESC LIMIT ? OFFSET ?" per_page (- page 1)]
      (doall res))))

(defn insert-photo [name filename description userid]
  (sql/with-connection db
    (sql/insert-record 
      :photos
      {:name name :filename filename :description description :userid userid})))

(defn get-user [username]
  (sql/with-connection db
    (sql/with-query-results
      res ["SELECT * FROM users WHERE username = ?" username]
      (first res))))

(defn insert-user [username password]
  (sql/with-connection db
    (sql/insert-record
      :users {:username username :password (crypt/encrypt password)})))
