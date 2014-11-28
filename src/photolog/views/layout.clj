(ns photolog.views.layout
  (:require [hiccup.page :refer [html5 include-css]]
            [noir.session :as session]))

(defmacro render-flash [ses-key string]
  `(if-let [key# (session/flash-get ~ses-key)]
     [:h3 (str ~string key#)]))

(defn base [& body]
  (html5
    [:head
     [:title "Welcome to photolog"]
     (include-css "/css/screen.css")]
    [:body
     (render-flash :notice "Notifications: ")
     (render-flash :error "Errors: ")
     body]))

(defn common [& body]
  (base body))

(defn admin [& body]
  (base 
    [:h1 "Admin"]
    body))
