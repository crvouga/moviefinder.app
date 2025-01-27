(ns moviefinder-app.home.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.frontend.db :as db]
   [core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [moviefinder-app.media.media-db.interface :as media-db]
   [moviefinder-app.frontend.config :refer [config]]
   [moviefinder-app.frontend.store :as store]))

(def popular-media-query
  {:query/limit 10
   :query/offset 0
   :query/select [:media/id :media/title :media/year :media/popularity :media/genre-ids :media/poster-url]
   :query/order [:media/popularity :desc]
   :query/where [:query/and
                 [:> :media/popularity 80]
                 [:= :media/media-type :media-type/movie]]})


(def query-result-chan! (media-db/query-result-chan! (merge config popular-media-query)))

(db/put-query-result! query-result-chan!)

(screen/register!
 :screen/home
 (fn [i]
   (let [query-result (db/to-query-result i popular-media-query)]
     [:div.w-full.flex-1.flex.flex-col.overflow-hidden
      [top-bar/view {:top-bar/title "Home"}]
      #_[:code (->> query-result :query-result/rows (take 1) pr-str)]
      [:div.w-full.flex-1.overflow-hidden
       [:swiper-container {:class "w-full h-full" :direction :vertical}
        (for [row (:query-result/rows query-result)]
          ^{:key row}
          [:swiper-slide {}
           [:button.w-full.h-full.overflow-hidden.cursor-pointer
            {:on-click #(store/put! i [:screen/clicked-link [:screen/media-details (select-keys row [:media/id])]])}
            [:img.w-full.h-full.object-cover {:src (:media/poster-url row) :alt (:media/title row)}]]])]]

      [top-level-bottom-buttons/view i]])))
