(ns app.media.media-db.inter.spec-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [cljs.spec.alpha :as s]
            [clojure.core.async :refer [go <!]]
            [app.media.media-db.inter :as media-db]
            [app.media.media-db.backend]
            [app.media.media-db.inter.fixture :as fixture]))


(defn test-query [config]
  (merge config {:query/limit 10
                 :query/offset 0}))

(deftest query-result-spec-test
  (testing "query-result-chan! returns spec valid response"
    (async done
           (go
             (doseq [config fixture/configs-read-only]
               (let [query (test-query config)
                     result (<! (media-db/query-result-chan! config query))]
                 (is (s/valid? :query-result/query-result result)
                     (str "Invalid query result for implementation " (:media-db/impl config)))))
             (done)))))

(deftest query-result-rows-test
  (testing "query-result-chan! returns non-empty results"
    (async done
           (go
             (doseq [config fixture/configs-read-only]
               (let [_put-result (<! (media-db/upsert-chan! (assoc config :media/entity fixture/test-media)))
                     query (test-query config)
                     result (<! (media-db/query-result-chan! config query))]
                 (is (seq (:query-result/rows result))
                     (str "Query result should not be empty for implementation " (:media-db/impl config)))))
             (done)))))

(defn valid-url? [url]
  (try
    (when url
      (let [url-obj (js/URL. url)]
        (and (contains? #{"http:" "https:"} (.-protocol url-obj))
             (not= "" (.-host url-obj)))))
    (catch :default _
      false)))

(deftest query-result-urls-test
  (testing "query-result-chan! returns results with valid poster and backdrop URLs"
    (async done
           (go
             (doseq [config fixture/configs-read-only]
               (let [query (test-query config)
                     result (<! (media-db/query-result-chan! config query))]
                 (doseq [row (:query-result/rows result)]
                   (is (valid-url? (:media/poster-url row))
                       (str "Invalid or missing poster URL for implementation " (:media-db/impl config)))
                   (is (valid-url? (:media/backdrop-url row))
                       (str "Invalid or missing backdrop URL for implementation " (:media-db/impl config))))))
             (done)))))