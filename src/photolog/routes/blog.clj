(ns photolog.routes.blog
  (:require [compojure.core :refer :all]
            [noir.response :as resp]
            [photolog.models.db :as db]
            [photolog.views.blog :as blog])
  (:import [java.io File]))

(defn home [req]
  (let [albums (db/get-albums-with-photos :page (:page req) :per_page 10)
        pagination {:current (get req :page 1) :per 10 
                    :total @db/album-count-guest}]
  (blog/blog-home albums pagination)))

(defroutes blog-routes
  (GET "/" {pars :params} (home pars)))
