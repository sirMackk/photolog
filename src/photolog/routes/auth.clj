(ns photolog.routes.auth
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.response :refer [redirect]]))

(defn login-page []
  ; add layout/common or base
  [:h1 "Login"])

(defn handle-login [user pass pass1]
  (prn (str "loggin in user " user)))

(defn handle-logout []
  (prn (str "loggin out user " (session/get :username))))

(defroutes auth-routes
  (GET "/gory" [] (login-page))
  (POST "/gory" [username password password1] (handle-login))
  (GET "/zmyk" [] (handle-logout)))
