(ns photolog.views.blog
  (:require [hiccup.element :refer [image link-to]]
            [photolog.utils :refer [albums slugidize friendly-date]]
            [photolog.views.layout :as layout])
  (:import [java.io File]))

(defn paginate [{:keys [current per total]}]
  (let [pages (int (Math/ceil (/ (Integer. total) (Integer. per))))
        current (Integer. current)]
    (if (> pages 1)
      (list
        (if (not= current 1)
          (list
            (link-to "/?page=1" "<<")
            (link-to (str "/?page=" (dec current)) "<")))
        [:p (str "Page " current " of " pages)]
        (if (not= current pages)
          (list
            (link-to (str "/?page=" (inc current)) ">")
            (link-to (str "/?page=" pages) ">>")))))))

(defn blog-home [album-batch pagination]
  (layout/common 
    [:nav
      (link-to "/about" "About")
     [:div {:class "paginate-me"}
      (paginate pagination)]]
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
            [:p (friendly-date (:created_at (first album-photos)))]]]
          [:div {:class "photos"}
           (for [photo album-photos]
             (list
               [:div {:class "photo"}
                 (image (str "/" albums File/separator (slugidize (:name photo)) File/separator "thumb_" (:filename photo)) (:description_2 photo))
                [:div {:class "photo-title"}
                 (:name_2 photo)]]))]]))
     [:div {:class "light-box"}
      [:div {:class "panel"}
       [:a {:href "javascript:;"} "Close"]
       [:p]]]]))

