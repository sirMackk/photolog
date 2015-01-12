(ns photolog.routes.admin
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
           [noir.io :refer [upload-file resource-path]]
           [noir.session :as session]
           [noir.response :as resp]
           [noir.util.route :refer [def-restricted-routes]]
           [ring.util.response :refer [file-response]]
           [photolog.utils :refer :all]
           [photolog.models.db :as db]
           [photolog.views.layout :as layout]
           [photolog.views.admin :as admin-views])
  (:import [java.io File]))
            
(defn admin-index [req]
  (let [albums (db/get-albums :page (:page req) :per_page 30)
        pagination {:current (get req :page 1) :per 30 
                    :total (:total_count (first albums))}]
    (prn (str "panie" albums))
    (admin-views/admin-index req albums pagination)))

(defn admin-edit-album [id]
  (if-let [album (db/get-album-with-photos id)]
    (admin-views/admin-edit-album id album)
    (do 
      (session/flash-put! :error "No such album!!")
      (resp/redirect "/admin"))))

(defn handle-file-upload [form album files]
  (try
    (if 
      (and 
        (not-empty files) 
        (not (nil? (first files))) 
        (not= (:size (first files)) 0))
      (doseq [photo files]
        (let [file (prozess-file photo)]
          (upload-file 
            (str albums File/separator (slugidize (get form :name))) file :create-path? true)
          (db/insert-photo-into-album file (get album :id))
          (save-thumbnail file (get form :name)))))
    (catch Exception ex
      (str "Error uploading file: " (.getMessage ex)))))


(defn admin-new-album-post [form files]
  (let [new_album (db/insert-album form (session/get :user)) files (format-files files)]
    (handle-file-upload form new_album files)
    (resp/redirect "/admin")))

    
(defn admin-edit-album-post [form multip]
  (let [_ (db/update-album form (session/get :user)) album (db/get-album (:id form)) files (format-files multip)]
    (handle-file-upload form album files)
    (do 
      (session/flash-put! :notice "Album updated") 
      (resp/redirect (str "/admin/" (get album :id) "/edit")))))

(defn delete-items [id_keys fn redirect-url]
  (let [ids (map #(Integer. %) id_keys)]
    (fn ids)
    (do
      (session/flash-put! :notice "Item(s) deleted!")
      (resp/redirect redirect-url))))

(defn admin-delete-album [form]
  (delete-items (keys (:albums form)) db/delete-albums "/admin"))

(defn admin-delete-photos [form]
  (delete-items 
    (keys (:photos_del form)) 
    db/delete-photos 
    (str "/admin/" (:album_id form) "/edit")))

(defn serve-photo [album-name photo-filename]
  (file-response (str albums File/separator album-name File/separator thumb-prefix photo-filename)))

(def-restricted-routes admin-routes
  (GET "/admin" {pars :params} (admin-index pars))
  (GET "/admin/new-album" [] (admin-views/admin-new-album))
  (POST "/admin/new-album" {form :params {multip "photos"} :multipart-params} (admin-new-album-post form multip)) 
  (GET "/admin/:album-id/edit" [album-id] (admin-edit-album album-id))
  (POST "/admin/edit" {form :params {multip "photos"} :multipart-params} (admin-edit-album-post form multip))
  (POST "/admin/delete-albums" {form :params} (admin-delete-album form))
  (POST "/admin/delete-photos" {form :params} (admin-delete-photos form))
  (GET "/albums/:album-name/:photo-filename" [album-name photo-filename] (serve-photo album-name photo-filename)))
