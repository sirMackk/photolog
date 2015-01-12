(ns photolog.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.logger :as logger]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [noir.util.middleware :as noir-mid]
            [noir.session :as session]
            [taoensso.timbre :as timbre]
            [com.postspectacular.rotor :as rotor]
            [compojure.route :as route]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [environ.core :refer [env]]
            [photolog.routes.admin :refer [admin-routes]]
            [photolog.routes.home :refer [home-routes]]
            [photolog.routes.auth :refer [auth-routes]]))

(defn init []
  (println "photolog is starting"))

(defn destroy []
  (println "photolog is shutting down"))

(timbre/set-config!
  [:appenders :rotor]
  {:min-level (env :log-level)
   :async false
   :enabled? (env :log-enabled)
   :max-message-per-msecs nil
   :fn rotor/append})

(timbre/set-config!
  [:shared-appender-config :rotor]
  {:path (env :log-path)
   :max-size (env :log-size)
   :backlog (env :log-backlog)})

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

;TODO Add error and other loggers here + rotor for output
(defn logga [app]
  (logger/wrap-with-logger app
    :info (fn [x] (timbre/info x))
    :debug (fn [x] (timbre/debug x))
    :error (fn [x] (timbre/error x))))

(def app 
    (noir-mid/app-handler [home-routes auth-routes admin-routes app-routes]
                                ;:middleware [wrap-anti-forgery wrap-multipart-params logger/wrap-with-logger]
                                :middleware [wrap-anti-forgery wrap-multipart-params logga]
                                :access-rules [{:uri "/admin*" :rule admin-access}]))
