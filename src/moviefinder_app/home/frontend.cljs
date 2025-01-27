(ns moviefinder-app.home.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.frontend.db :as db]
   [core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [moviefinder-app.media.media-db.interface :as media-db]
   [moviefinder-app.frontend.ctx :refer [ctx]]))

(def popular-media-query
  {:query/limit 10
   :query/offset 0
   :query/select [:media/id :media/title :media/year :media/popularity :media/genre-ids :media/poster-url]
   :query/order [:media/popularity :desc]
   :query/where [:query/and
                 [:> :media/popularity 80]
                 [:= :media/media-type :media-type/movie]]})


(def query-result-chan! (media-db/query-result-chan! (merge ctx popular-media-query)))

(db/put-query-result! query-result-chan!)

(screen/register!
 :screen/home
 (fn [i]
   [:div.w-full.flex-1.flex.flex-col.overflow-hidden
    [top-bar/view {:top-bar/title "Home"}]
    (let [query-result (db/to-query-result i popular-media-query)]
      [:div.w-full.flex-1.overflow-y-auto
       (for [row (:query-result/rows query-result)]
         ^{:key row}
         [:div.flex.flex-col.p-4
          [:h3 (:media/title row)]
          [:p (:media/year row)]
          [:p (:media/popularity row)]
          [:p (:media/genre-ids row)]
          #_[:img {:src (:media/poster-url row) :alt (:media/title row) :width 200 :height 300}]])])
    [top-level-bottom-buttons/view i]]))
