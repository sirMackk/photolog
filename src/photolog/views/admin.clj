(ns photolog.views.admin
  (:require [hiccup.form :refer :all]
            [hiccup.element :refer [image link-to]]
            [photolog.utils :refer [albums slugidize]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [photolog.views.layout :as layout])
  (:import [java.io File]))

(defn album-form [url & [val-map]]
  [:div.album-form
    (form-to {:enctype "multipart/form-data"}
             [:post url] 
             (anti-forgery-field)
             (label "name" "name")
             (text-field "name" (:name val-map))
             (label "desc" "description")
             (text-area "description" (:description val-map))
             (label "photos" "photos")
             (file-upload {:multiple "multiple"} "photos")
             (label "status" "status")
             (drop-down "status" 
                        [["draft" 0] ["published" 1]] 
                        (get val-map :status 0))
             (if (contains? val-map :id)
               (hidden-field "id" (:id val-map)))
             (submit-button "Submit"))])

(defn admin-index [r album]
  (layout/admin
    [:h1 "Albums index"]
    (form-to [:post "/admin/delete-albums"]
      (submit-button "Delete selected albums")
      (anti-forgery-field)
      [:table.admin-albums
       [:thead
        [:th "Name"]
        [:th "Desc"]
        [:th "Status"]
        [:th "Created at"]
        [:th "Updated at"]
        [:th "Actions"]]
       [:tbody
         (for [{:keys [created_at updated_at name description status id]} album]
           [:tr
            [:td.name name]
            [:td.desc description]
            [:td.status status]
            [:td.timestamp created_at]
            [:td.timestamp updated_at]
            [:td.actions
             (link-to (str "/admin/" id "/edit") "Edit")
             (label (str "del_" id) "Delete?")
             (check-box (str "albums[" id "]"))]])]])))

(defn admin-new-album []
  (layout/admin
    (album-form "/admin/new-album")))

(defn admin-edit-album [id album]
  (layout/admin
    (album-form (str "/admin/edit") (first album))
    [:hr]
    (form-to [:post "/admin/delete-photos"]
      (anti-forgery-field)
      (hidden-field "album_id" id)
      (for [photo album]
        (do 
          [:div.photo
            (label (str "photos_del_[" id "]") "Delete?")
            (check-box (str "photos_del[" (:id_2 photo) "]"))
            (image (str "/" albums File/separator (slugidize (:name photo)) File/separator (:filename photo)))]))
      (submit-button "Delete selected photos"))))
