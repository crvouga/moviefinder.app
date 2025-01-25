(ns moviefinder-app.home.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.frontend.db :as db]
   [moviefinder-app.core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [moviefinder-app.media.media-db.interface :as media-db]
   [moviefinder-app.frontend.ctx :refer [ctx]]
   [clojure.core.async :refer [go-loop <! >!]]
   [moviefinder-app.frontend.store :as store]))

(def popular-media-query
  {:query/limit 10
   :query/offset 0
   :query/select [:media/title :media/year :media/popularity :media/genre-ids :media/poster-url]
   :query/where [:and
                 [:> :media/popularity 80]
                 [:= :media/media-type :media-type/movie]]
   :query/order [:media/popularity :desc]})

(def popular-media-query-msg-chan!
  (-> popular-media-query
      (merge ctx)
      (media-db/query-chan!)))

(go-loop []
  (when-let [result (<! popular-media-query-msg-chan!)]
    (>! store/msg-chan! [:db/got-query-result result])
    (recur)))

(screen/reg!
 :screen/home
 (fn [i]
   [:div.w-full.flex-1.flex.flex-col
    [top-bar/view {:top-bar/title "Home"}]
    (let [query-result (db/to-query-result i popular-media-query)]
      [:div.w-full.flex-1
       [:code (pr-str query-result)]])
    [top-level-bottom-buttons/view i]]))