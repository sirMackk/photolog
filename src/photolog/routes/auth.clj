(ns photolog.routes.auth
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.response :refer [redirect]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.form :refer [form-to text-field password-field label submit-button]]
            [photolog.models.db :as db]
            [photolog.views.layout :as layout]))

(defn login-page []
  (layout/common
    (form-to [:post "/gory"]
             (anti-forgery-field)
             (label "user" "user")
             (text-field "user")
             (label "pass" "password")
             (password-field "pass")
             (submit-button "Descend"))))

(defn handle-login [user pass]
  (let [user (db/get-user user)]
    (if (and user (crypt/compare pass (:password user)))
      (do (session/put! :user user) (redirect "/admin"))
      (do (session/flash-put! :error "Nope") (redirect "/")))))



(defn handle-logout []
  (session/clear!)
  (session/flash-put! :notice "Logged out!")
  (redirect "/"))

(defroutes auth-routes
  (GET "/gory" [] (login-page))
  (POST "/gory" [username password] (handle-login username password))
  (GET "/zmyk" [] (handle-logout)))
