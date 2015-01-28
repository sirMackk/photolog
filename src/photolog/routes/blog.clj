(ns photolog.routes.blog
  (:require [compojure.core :refer :all]
            [noir.response :as resp]
            [photolog.models.db :as db]
            [photolog.views.blog :as blog])
  (:import [java.io File]))

(defn home []
  (blog/blog-home (db/get-albums-with-photos)))

(defn get-albums [page per-page]
  (resp/json (partition-by :id (db/get-albums-with-photos :page page :per_page per-page))))

(defroutes blog-routes
  (GET "/" [] (home))
  (GET "/albums" [page per-page] (get-albums page per-page)))
