(ns photolog.routes.admin
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
           [hiccup.form :refer :all]
           [hiccup.element :refer [image link-to]]
           [noir.io :refer [upload-file resource-path]]
           [noir.session :as session]
           [noir.response :as resp]
           [ring.util.anti-forgery :refer [anti-forgery-field]]
           [ring.util.response :refer [file-response]]
           [photolog.utils :refer [thumb-prefix albums]]
           [photolog.models.db :as db]
           [photolog.views.layout :as layout]
           [clojure.string :as st]
           [clojure.java.io :as io])
  (:import [java.io File FileInputStream FileOutputStream]
           [java.awt.image AffineTransformOp BufferedImage]
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO))
            

; these can go live in like album utils yo

(defn slugidize [string]
  (st/replace (st/trim (st/lower-case string)) #"[^a-z0-9-]+" "-"))

(defn scale [img ratio width height]
  "Creates an AffineTransform Scale object using the ration, then, feeds that
  to the AffineTransfromOp constructor. Finally, the filter method of transform-op
  is called with the img BufferedImage as src and a new BufferedImage object
  as the dst."
  (let [scale (AffineTransform/getScaleInstance (double ratio) (double ratio))
        transform-op (AffineTransformOp. scale AffineTransformOp/TYPE_BILINEAR)]
    (.filter transform-op img (BufferedImage. width height (.getType img)))))

(defn scale-image [file]
  "Creates a BufferedImage object using ImageIO and sets the correct ratios"
  (let [img (ImageIO/read file)
        img-width (.getWidth img)
        img-height (.getHeight img)
        ratio 0.5]
    (scale img ratio (int (* img-width ratio)) (int (* img-height ratio)))))

(defn save-thumbnail [{:keys [filename]} album-name]
  "Creates an empty file (File.) and uses ImageIO/write
  to write BufferedImage into that file."
  (let [path (str albums File/separator (slugidize album-name) File/separator)]
    (with-open [output_image (io/input-stream (str path filename))]
      (ImageIO/write
        (scale-image output_image)
        "jpeg"
        (File. (str path thumb-prefix filename))))))

(defn admin-index [r]
  (layout/admin
    [:ul.albums
     (for [{:keys [created_at updated_at name description id]} (db/get-albums :page (:page r) :per_page 30)]
       [:li
        (link-to (str "/admin/" id) name)
        [:div.stamps (str created_at " / " updated_at)]
        [:div.desc description]])]))

(defn album-form [url & [val-map]]
  (form-to {:enctype "multipart/form-data"}
           [:post url] 
           (anti-forgery-field)
           (label "name" "name")
           (text-field "name" (:name val-map))
           (label "desc" "description")
           (text-area "description" (:description val-map))
           (label "photos" "photos")
           (file-upload {:multiple "multiple"} "photos")
           (submit-button "Submit")))

(defn admin-new-album []
  (layout/admin
    (album-form "/admin-new-album")))

(defn admin-new-album-post [form photos]
  (try
    (let [new_album (db/insert-album form (session/get :user))]
      (doseq [photo photos]
        (upload-file (str albums File/separator (slugidize (get form :name))) photo :create-path? true)
        (db/insert-photo-into-album photo (get new_album :id))
        (save-thumbnail photo (get form :name))))
    (catch Exception ex
      (str "Error uploading file: " (.getMessage ex))))
  (resp/redirect "/admin"))


(defn admin-show-album [id]
  (let [album (db/get-album-with-photos id)]
    (if album
      (layout/admin
        [:h1 (str (:name (first album)))]
        [:p (str (:description (first album)))]
        (for [photo album]
          (do 
            [:h3 (str (:name_2 photo))]
            [:p (str (:created_at_2 photo))]
            [:p (str (:description_2 photo) " album name: " (:filename photo))]
            (image (str "/" albums File/separator (slugidize (:name photo)) File/separator (:filename photo)))))) ; pull out into own function?
      (do (session/flash-put! :error "No such album!") (resp/redirect "/admin")))))

(defn serve-photo [album-name photo-filename]
  (file-response (str albums File/separator album-name File/separator thumb-prefix photo-filename)))


(defn admin-edit-album [id]
  (let [album (db/get-album-with-photos id)]
    (if album
      (layout/admin
        (album-form (str "/admin/" (:id album) "/edit") (first album))
        [:hr]
        (for [photo album]
          (do 
            (prn photo)
            [:div.photo
             (check-box (:id_2 photo))
            (image (str "/" albums File/separator (slugidize (:name photo)) File/separator (:filename photo)))])))
      (do (session/flash-put! :error "No such album!") (resp/redirect "/admin")))))

(defn admin-edit-album-post [form multip]
  (let [album (db/update-album form (session/get :user))]
    (try
      (if-not (empty? multip)
        ; this can be pulled out and shared with new-album
        (doseq [photo multip]
          (upload-file (str albums File/separator (slugidize (get form :name))) photo :create-path? true)
          (db/insert-photo-into-album photo (get album :id))
          (save-thumbnail photo (get form :name))))
      (catch Exception ex
        (str "Error updating album: " (.getMessage ex))))))

(defn admin-delete-album [ids]
  ; delete an album along with all child images
  ; show js confirmation dialog
  )

(defn admin-delete-images [ids]
  ; delete images
  )

(defroutes admin-routes
  (GET "/admin" {pars :params} (admin-index pars))
  (GET "/admin/new-album" [] (admin-new-album))
  (POST "/admin/new-album" {form :params {multip "photos"} :multipart-params} (admin-new-album-post form multip)) 
  (GET "/admin/:album-id" [album-id] (admin-show-album album-id))
  (GET "/admin/:album-id/edit" [album-id] (admin-edit-album album-id))
  (POST "/admin/edit" {form :params {multip "photos"} :multipart-params} (admin-edit-album-post form multip))
  (DELETE "/admin/:album-id/delete" [ids] (admin-delete-images ids))
  (DELETE "/admin/" [ids] (admin-delete-album ids))
  (GET "/albums/:album-name/:photo-filename" [album-name photo-filename] (serve-photo album-name photo-filename)))
