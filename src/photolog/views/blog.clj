(ns photolog.views.blog
  (:require [hiccup.element :refer [image link-to]]
            [photolog.utils :refer [albums slugidize]]
            [photolog.views.layout :as layout])
  (:import [java.io File]))

(defn blog-home [album-batch]
  (layout/common 
    [:nav
      (link-to "/about" "About")]
    [:header
      [:h1
        (link-to "/" "Photolog")]]

    [:div {:class "wrapper"}
     (for [album-photos (partition-by :id album-batch)]
       (list 
         [:div {:class "album"}
          [:div {:class "album-meta"}
           [:h4 (:name (first album-photos))]
           [:p (:description (first album-photos))]
           [:div {:class "date"}
            [:p (:created_at (first album-photos))]]] ; add date formatter
          [:div {:class "photos"}
           (for [photo album-photos]
             (list
               [:div {:class "photo"}
                 (image (str "/" albums File/separator (slugidize (:name photo)) File/separator (:filename photo)) (:description_2 photo))
                [:div {:class "photo-title"}
                 (:name_2 photo)]]))]]))]))

