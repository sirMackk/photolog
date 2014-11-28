(ns photolog.routes.admin
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
           [hiccup.form :refer :all]
           [hiccup.element :refer [image link-to]]
           [noir.io :refer [upload-file resource-path]]
           [noir.session :as session]
           [ring.util.anti-forgery :refer [anti-forgery-field]]
           [photolog.utils :refer [thumb-prefix albums]]
           [photolog.models.db :refer [get-albums]]
           [photolog.views.layout :as layout]
           [clojure.java.io :as io])
  (:import [java.io File FileInputStream FileOutputStream]
           [java.awt.image AffineTransformOp BufferedImage]
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO))
            

; these can go live in like album utils yo
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

(defn save-thumbnail [{:keys [filename]}]
  "Creates an empty file (File.) and uses ImageIO/write
  to write BufferedImage into that file."
  (let [path (str albums File/separator)]
    (with-open [output_image (io/input-stream (str path filename))]
      (ImageIO/write
        (scale-image output_image)
        "jpeg"
        (File. (str path thumb-prefix filename))))))

(defn admin-index [r]
  (layout/admin
    [:ul.albums
     (for [{:keys [created_at updated_at name description id]} (get-albums :page (:page r))]
       [:li
        (link-to (str "/admin/" id) name)
        [:div.name name]
        [:div.stamps (str created_at " / " updated_at)]
        [:div.desc description]])]))

(defn admin-new-album []
  (layout/admin
    (form-to {:enctype "multipart/form-data"}
             [:post "/admin/new-album"]
             (anti-forgery-field)
             (label "name" "name")
             (text-field "name")
             (label "desc" "description")
             (text-area "desc")
             (label "photos" "photos")
             (file-upload {:multiple "multiple"} "photos")
             (submit-button "Create album"))))

(defn  admin-new-album-post [form photos]
  (try
    (doseq [photo photos]
      (prn (get photo :filename))
      (prn albums)
      (noir.io/upload-file albums photo :create-path? true))
    (catch Exception ex
      (str "Error uploading file: " (.getMessage ex)))))
      ;thumbnail
      ;add to db
    ;


(defn admin-show-album [id]
  ; retrieve and render album
  )

(defn admin-edit-album [id]
  ; retreive album and reuse create form to allow for rendering of album
  )

(defn admin-delete-album [ids]
  ; delete an album along with all child images
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
  (DELETE "/admin/:album-id/delete/" [ids] (admin-delete-images ids))
  (DELETE "/admin/" [ids] (admin-delete-album ids)))
