(ns photolog.routes.blog
  (:require [compojure.core :refer :all]
            [hiccup.element :refer [image link-to]]
            [noir.response :as resp]
            [photolog.utils :refer [albums slugidize]]
            [photolog.models.db :as db]
            [photolog.views.layout :as layout])
  (:import [java.io File]))

(defn home []
  (layout/common 
    [:h1 "Blog"]
    (prn (db/get-albums-with-photos))
    (for [album-photos (partition-by :id (db/get-albums-with-photos))]
      (list* 
        [:h2 (:name (first album-photos))]
        (for [photo album-photos]
          (list*
          (image (str "/" albums File/separator (slugidize (:name photo)) File/separator (:filename photo)))
          [:h4 (:name_2 photo)]))))))

(defn get-albums [page]
  (resp/json (partition-by :id (db/get-album-with-photos :page page))))

      

(defroutes blog-routes
  (GET "/" [] (home))
  (GET "/albums" [page] (get-albums page)))
