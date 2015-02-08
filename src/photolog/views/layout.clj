(ns photolog.views.layout
  (:require [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer [link-to]]
            [noir.session :as session]))

(defmacro render-flash [ses-key string]
  `(if-let [key# (session/flash-get ~ses-key)]
     [:h3 (str ~string key#)]))

(defn blog-head []
   (include-js "/js/blog.min.js"))


(defn base [body & head]
  (html5
    [:head
     [:title "Photolog"]
     (include-css "/css/app.min.css")
     [:meta {:name "viewport" :content "width=device-width"}]
     head]


    [:body#top
     (render-flash :notice "Notifications: ")
     (render-flash :error "Errors: ")
     [:div.wrapper
     body]]))

(defn admin-panel []
  [:div.admin-panel
   [:h2 (str "Welcome, " (:username (session/get :user)))]
   [:div.admin-controls
    (link-to "/zmyk" "Zmyk")
    (link-to "/admin/new-album" "New Album")]])

(defn common [& body]
  (base body (blog-head)))

(defn admin [& body]
  (base 
    [:header
      (admin-panel)]
    [:div.admin-body
     [:div.left-floater
      [:a {:href "#top"} "U"]]
      body]))
