(ns lib.http-cookie-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [lib.http-cookie :as http-cookie]))

(deftest to-header-value-test
  (testing "Basic cookie with only name and value"
    (let [cookie {:cookie/name "session-id"
                  :cookie/value "abc123"}
          header-value (http-cookie/to-header-value cookie)]
      (is (= "session-id=abc123; Path=/; HttpOnly" header-value))))

  (testing "Cookie with path"
    (let [cookie {:cookie/name "user"
                  :cookie/value "john"
                  :cookie/path "/account"}
          header-value (http-cookie/to-header-value cookie)]
      (is (= "user=john; Path=/account; HttpOnly" header-value))))

  (testing "Cookie with default path when nil"
    (let [cookie {:cookie/name "user"
                  :cookie/value "john"
                  :cookie/path nil}
          header-value (http-cookie/to-header-value cookie)]
      (is (= "user=john; Path=/; HttpOnly" header-value))))

  (testing "Cookie with domain"
    (let [cookie {:cookie/name "lang"
                  :cookie/value "en"
                  :cookie/domain "example.com"}
          header-value (http-cookie/to-header-value cookie)]
      (is (= "lang=en; Path=/; Domain=example.com; HttpOnly" header-value))))

  (testing "Cookie with max-age"
    (let [cookie {:cookie/name "theme"
                  :cookie/value "dark"
                  :cookie/max-age 86400}]
      (is (= "theme=dark; Path=/; Max-Age=86400; HttpOnly" (http-cookie/to-header-value cookie)))))

  (testing "Secure cookie"
    (let [cookie {:cookie/name "auth"
                  :cookie/value "token"
                  :cookie/secure true}]
      (is (= "auth=token; Path=/; Secure; HttpOnly" (http-cookie/to-header-value cookie)))))

  (testing "HttpOnly cookie (explicit true)"
    (let [cookie {:cookie/name "auth"
                  :cookie/value "token"
                  :cookie/http-only true}]
      (is (= "auth=token; Path=/; HttpOnly" (http-cookie/to-header-value cookie)))))

  (testing "HttpOnly cookie (default when nil)"
    (let [cookie {:cookie/name "auth"
                  :cookie/value "token"
                  :cookie/http-only nil}]
      (is (= "auth=token; Path=/; HttpOnly" (http-cookie/to-header-value cookie)))))

  (testing "Non-HttpOnly cookie"
    (let [cookie {:cookie/name "auth"
                  :cookie/value "token"
                  :cookie/http-only false}]
      (is (= "auth=token; Path=/" (http-cookie/to-header-value cookie)))))

  (testing "Cookie with SameSite"
    (let [cookie {:cookie/name "session"
                  :cookie/value "xyz789"
                  :cookie/same-site "Strict"}]
      (is (= "session=xyz789; Path=/; HttpOnly; SameSite=Strict" (http-cookie/to-header-value cookie)))))

  (testing "Cookie with multiple attributes"
    (let [cookie {:cookie/name "session"
                  :cookie/value "xyz789"
                  :cookie/path "/"
                  :cookie/domain "example.org"
                  :cookie/max-age 3600
                  :cookie/secure true
                  :cookie/http-only true
                  :cookie/same-site "Lax"}]
      (is (= "session=xyz789; Path=/; Domain=example.org; Max-Age=3600; Secure; HttpOnly; SameSite=Lax"
             (http-cookie/to-header-value cookie))))))