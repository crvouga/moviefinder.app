(ns moviefinder-app.login.login-with-email.use-login-link-test
  (:require [clojure.test :refer [deftest is testing]]
            [moviefinder-app.error :refer [thrown-err?]]
            [moviefinder-app.login.login-with-email.login-link.login-link-db :as login-link-db]
            [moviefinder-app.login.login-with-email.login-link.login-link :as login-link]
            [moviefinder-app.user-session.user-session-db :as user-session-db]
            [moviefinder-app.user.user-db :as user-db]
            [moviefinder-app.user.user :as user]
            [moviefinder-app.login.login-with-email.send-login-link :refer [send-login-link!]]
            [moviefinder-app.login.login-with-email.use-login-link :refer [use-login-link!]]
            [moviefinder-app.deps :as deps]
            [moviefinder-app.session :as session]))


(defn fixture []
  (merge (deps/deps-test)
         {:user/email (user/random-email!)
          :session/id (session/random-session-id!)}))

(deftest use-login-link-test
  (testing "use login link"
    (let [f (fixture)
          login-link (send-login-link! f)
          _ (use-login-link! (merge f login-link))
          after (first (login-link-db/find-by-email! (f :login-link-db/login-link-db) (:user/email f)))]
      (is (not (nil? (after :login-link/used-at-posix))))))


  (testing "user should be logged in after using login link"
    (let [f (fixture)
          login-link (send-login-link! f)
          before (user-session-db/find-by-session-id!
                  (f :user-session-db/user-session-db)
                  (f :session/id))
          _ (use-login-link! (merge f login-link))
          after (user-session-db/find-by-session-id!
                 (f :user-session-db/user-session-db)
                 (f :session/id))]
      (is (= before #{}))
      (is (= (count after) 1))))

  (testing "it should create a new user if the email is not found"
    (let [f (fixture)
          login-link (send-login-link! f)
          before (first (user-db/find-by-email! (f :user-db/user-db) (:user/email f)))
          _ (use-login-link! (merge f login-link))
          after (first (user-db/find-by-email! (f :user-db/user-db) (:user/email f)))]
      (is (nil? before))
      (is (not (nil? after)))))

  (testing "it should NOT create a new user if already exists"
    (let [f (fixture)
          login-link (send-login-link! f)
          user (user/new! f)
          _ (user-db/put! (f :user-db/user-db) #{user})
          before (user-db/find-by-email! (f :user-db/user-db) (:user/email f))
          _ (use-login-link! (merge f login-link))
          after (user-db/find-by-email! (f :user-db/user-db) (:user/email f))]
      (is (= before after))
      (is (= after #{user}))))

  (testing "it should error if the login link is already used"
    (let [f (fixture)
          login-link (send-login-link! f)
          input (merge f login-link)
          _ (use-login-link! input)
          _ (thrown-err? :err/login-link-already-used (use-login-link! input))]))

  (testing "it should error if the login link does not exist"
    (let [f (fixture)
          login-link-new (login-link/new! (user/random!))
          input (merge f login-link-new)
          _ (thrown-err? :err/login-link-not-found (use-login-link! input))]))

  (testing "it should error if login link is expired"
    (let [f (fixture)
          login-link (send-login-link! f)
          login-link-expired (login-link/mark-as-expired login-link)
          _ (login-link-db/put! (f :login-link-db/login-link-db) #{login-link-expired})
          input (merge f login-link-expired)
          _ (thrown-err? :err/login-link-expired (use-login-link! input))]))

  (testing "it should error if there is not session id to associate with the user"
    (let [f (fixture)
          login-link (send-login-link! f)
          input (-> (merge f login-link) (dissoc :session/id))
          _ (thrown-err? :err/user-session-id-not-associate-with-request (use-login-link! input))])))
    
