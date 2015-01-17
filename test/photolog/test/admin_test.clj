(ns photolog.test.admin-test
  (:use clojure.test
        photolog.handler
        photolog.models.db
        peridot.core
        photolog.test.helpers)
  (:require [clojure.java.io :refer [file]]))
;TODO Test photo deletion
;TODO Test photo desc/title edit

(defn clean-database [f]
  (drop-tables)
  (create-table-users)
  (create-table-albums)
  (create-table-photos)
  (create-test-user)
  (f))


(use-fixtures :each clean-database)

(def fixture "test/photolog/test/fixture_photo.jpg")

(deftest test-admin-index
  (testing "admin-index without params"
    (with-user 
      (fn [rsp]
        (let [uri (get-in rsp [:request :uri])]
          (is (= uri "/admin")))))))

(deftest test-admin-index-params
  (testing "admin-index with page param"
    (with-user
      (fn [rsp]
        ; response should be short because no albums at that offset
        (is (< (.length (get-in rsp [:response :body])) 1000)))
      (request "/admin?page=2"))))

(deftest test-admin-new-album
  (testing "admin-new-album"
    (with-user
      (fn [rsp]
        (is (get-in rsp [:response :status]) 200))
      (request "/admin/new-album"))))

(deftest test-admin-new-album-post
  (testing "admin-new-album-post"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (not (nil? (re-find #"album-test" body))))))
      (request "/admin/new-album" :request-method :post
                                  :params {:name "album-test"
                                           :description "album test desc"
                                           :status "1" })
      (follow-redirect))))

(deftest test-admin-edit-album
  (testing "admin-edit-album after creating album"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (not (nil? (re-find #"album-test" body))))))
      (request "/admin/new-album" :request-method :post
                                 :params {:name "album-test"
                                          :description "album test desc" })
      (request "/admin/1/edit"))))

(deftest test-admin-edit-update-album
  (testing "admin-edit-album-post update album"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (not (nil? (re-find #"album-test-updated" body))))))
      (request "/admin/new-album" :request-method :post
                                  :params {:name "album-test-updated"
                                           :description "album test desc" })
      (request "/admin/edit" :request-method :post
                             :params {:name "album-test-updated" 
                                      :description "wat"
                                      :id 1})
      (follow-redirect))))

(deftest test-admin-delete-album
  (testing "admin-delete-album"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (not (nil? (re-find #"Item\(s\) deleted" body))))
          (is (nil? (re-find #"album-test" body)))))
      (request "/admin/new-album" :request-method :post
                                  :params {:name "album-test"
                                           :description "album test desc" })
      (request "/admin/delete-albums" :request-method :post
                                      :body (.getBytes "albums[1]=true", "UTF-8"))
      (follow-redirect))))

(deftest create-album-with-photo
  (testing "create album with photo" 
    (let [f (file fixture)]
      (with-user
        (fn [rsp]
          (let [body (get-in rsp [:response :body])]
            (is (not (nil? (re-find #"fixture_photo" body))))))
        (request "/admin/new-album" :request-method :post
                                    :params {:name "album-test-photo"
                                             :description "album test desc" 
                                             :status "1"
                                             :photos f})
        (request "/admin/1/edit")))))

(deftest update-album-with-photo
  (testing "adding photo to existing album"
    (let [f (file fixture)]
      (with-user
        (fn [rsp]
          (let [body (get-in rsp [:response :body])]
            (is (not (nil? (re-find #"fixture_photo" body))))))
        (request "/admin/new-album" :request-method :post
                                    :params {:name "album-test"
                                             :description "album test desc" })
        (request "/admin/edit" :request-method :post
                               :params {:name "new-name"
                                        :description "New Desc"
                                        :id 1
                                        :photos f})
        (request "/admin/1/edit")))))

(deftest update-photo-fields
  (testing "updating a photos fields"
    (let [f (file fixture)]
      (with-user
        (fn [rsp]
          (let [body (get-in rsp [:response :body])]
            (is (not (nil? (re-find #"updated-name" body))))
            (is (not (nil? (re-find #"updated-desc" body))))))
        (request "/admin/new-album" :request-method :post
                                    :params {:name "album-test-photo"
                                             :description "album test desc"
                                             :status "1"
                                             :photos f})
        (request "/admin/update-photos" :request-method :post
                                        :body
                                          (.getBytes "album_id=1&photos_name[1]=photo-name&photos_desc[1]=photo-desc" "UTF-8"))
        (request "/admin/update-photos" :request-method :post
                                        :body
                                          (.getBytes "album_id=1&photos_name[1]=updated-name&photos_desc[1]=updated-desc" "UTF-8"))
        (request "/admin/1/edit")))))

(deftest update-photo-delete-photo
  (testing "deleting a photo from an album"
    (let [f (file fixture)]
      (with-user
        (fn [rsp]
          (let [body (get-in rsp [:response :body])]
            (is (nil? (re-find #"photo-name" body)))))
        (request "/admin/new-album" :request-method :post
                                    :params {:name "album-test-photo"
                                             :description "album test desc"
                                             :status "1"
                                             :photos f})
        (request "/admin/update-photos" :request-method :post
                                        :body
                                          (.getBytes "album_id=1&photos_name[1]=photo-name&photos_desc[1]=photo-desc" "UTF-8"))
        (request "/admin/update-photos" :request-method :post
                                        :body
                                          (.getBytes "album_id=1&photos_del[1]=true&photos_name[1]=photo-name" "UTF-8"))
        (request "/admin/1/edit")))))
