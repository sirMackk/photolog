(ns photolog.models.db
  (:require [clojure.java.jdbc :as sql]))

; figure out pagination
;

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

(defn get-photos []
  ; add pagination here using parameters, defaults, and sql
  (sql/with-connetion db
    (sql/with-query-results
      res ["SELECT * FROM photos ORDER BY created_at DESC"]
      (doall res))))






