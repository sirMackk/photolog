(ns photolog.handler
  (:require [compojure.core :refer [defroutes routes]]
            ;[ring.middleware.resource :refer [wrap-resource]]
            ;[ring.middleware.file-info :refer [wrap-file-info]]
            ;[hiccup.middleware :refer [wrap-base-url]]
            ;[compojure.handler :as handler]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [noir.util.middleware :as noir-mid]
            [noir.session :as session]
            [compojure.route :as route]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [photolog.routes.admin :refer [admin-routes]]
            [photolog.routes.home :refer [home-routes]]
            [photolog.routes.auth :refer [auth-routes]]))

(defn init []
  (println "photolog is starting"))

(defn destroy []
  (println "photolog is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn admin-access [_]
  (session/get :user))

; 
;(def app
  ;(-> (routes home-routes app-routes)
      ;(handler/site)
      ;(wrap-anti-forgery)
      ;(wrap-base-url)
      ;(wrap-session)))

(def app (noir-mid/app-handler [home-routes auth-routes admin-routes app-routes]
                                :middleware [wrap-anti-forgery wrap-multipart-params]
                                :access-rules [{:uri "/admin*" :rule admin-access}]))
