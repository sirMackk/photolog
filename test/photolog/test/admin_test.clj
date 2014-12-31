(ns photolog.test.admin-test
  (:use clojure.test
        ;ring.mock.request
        photolog.handler
        photolog.models.db
        peridot.core
        photolog.test.helpers))
  ;(:load "../src/photolog/models/db"))

(defn clean-database [f]
  (drop-tables)
  (create-table-users)
  (create-table-albums)
  (create-table-photos)
  (create-test-user)
  (f))


(use-fixtures :each clean-database)

(deftest test-admin
  (testing "admin-index without params"
    (with-user 
      (fn [rsp]
        (let [uri (get-in rsp [:request :uri])]
          (is (= uri "/admin"))))))

  (testing "admin-index with page param"
    (with-user
      (fn [rsp]
        ; response should be short because no albums at that offset
        (is (< (.length (get-in rsp [:response :body])) 1000)))
      (request "/admin?page=2")))

  (testing "admin-new-album"
    (with-user
      (fn [rsp]
        (is (get-in rsp [:response :status]) 200))
      (request "/admin/new-album")))

  (testing "admin-new-album-post"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (not (nil? (re-find #"album-test" body))) true)))
      (request "/admin/new-album" :request-method :post
                                  :params {:name "album-test"
                                           :description "album test desc" })
      (follow-redirect)))

  (testing "admin-show-album after creating album"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (not (nil? (re-find #"album test desc" body))) true)))
      (request "/admin/new-album" :request-method :post
                                  :params {:name "album-test"
                                           :description "album test desc"})
      (request "/admin/1")))

  (testing "admin-edit-album after creating album"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (not (nil? (re-find #"album-test" body))) true)))
      (request "/admin/new-album" :request-method :post
                                 :params {:name "album-test"
                                          :description "album test desc" })
      (request "/admin/1/edit")))

  (testing "admin-edit-album-post update album"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (not (nil? (re-find #"album-test-updated" body))) true)))
      (request "/admin/new-album" :request-method :post
                                  :params {:name "album-test-updated"
                                           :description "album test desc" })
      (request "/admin/edit" :request-method :post
                             :params {:name "album-test-updated" 
                                      :description "wat"
                                      :id 1})
      (follow-redirect)))

  (testing "admin-delete-album"
    (with-user
      (fn [rsp]
        (let [body (get-in rsp [:response :body])]
          (is (re-find #"album-test" body) nil)))
      (request "/admin/new-album" :request-method :post
                                  :params {:name "album-test"
                                           :description "album test desc" })
      (request "/admin/delete-albums" :request-method :post
                                      :body (.getBytes "albums[1]=true", "UTF-8"))
      (follow-redirect)))

          )
