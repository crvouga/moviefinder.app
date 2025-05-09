(ns app.media.media-id-test
  (:require
   [cljs.test :refer [deftest testing is]]
   [app.media.media-id :as media-id]))

(deftest from-tmdb-id-test
  (testing "media id from and to tmdb id"
    (let [tmdb-id "123"]
      (is (= tmdb-id (media-id/to-tmdb-id (media-id/from-tmdb-id tmdb-id)))))))
