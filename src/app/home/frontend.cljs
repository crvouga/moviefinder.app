(ns app.home.frontend
  (:require
   [app.frontend.screen :as screen]
   [app.frontend.db :as db]
   [core.ui.image :as image]
   [core.ui.image-preload :as image-preload]
   [core.ui.top-bar :as top-bar]
   [app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [app.frontend.config :refer [config]]
   [app.media.media-db.frontend]
   [app.media.media-db.interface :as media-db]
   [app.frontend.store :as store]))

(def popular-media-query
  {:query/limit 25
   :query/offset 0
   :query/select [:media/id :media/title :media/year :media/popularity :media/genre-ids :media/poster-url]
   :query/order [:media/popularity :desc]
   :query/where [:query/and
                 [:> :media/popularity 80]
                 [:= :media/media-type :media-type/movie]]})


(def query-result-chan! (media-db/query-result-chan! (merge config popular-media-query)))

(db/put-query-result! query-result-chan!)

(defn on-slide-click [i row]
  (store/put! i [:screen/clicked-link [:screen/media-details (select-keys row [:media/id])]]))

(defn view-swiper-slide [i row]
  [:swiper-slide {}
   [:button.w-full.h-full.overflow-hidden.cursor-pointer.select-none
    {:on-click #(on-slide-click i row)}
    [image-preload/view {:image/url (:media/backdrop-url row)}]
    [image/view {:image/url (:media/poster-url row)
                 :image/alt (:media/title row)
                 :class "pointer-events-none w-full h-full"}]]])

(defn view-swiper [i rows]
  [:swiper-container {:class "w-full flex-1 overflow-hidden" :direction :vertical}
   (for [row rows]
     ^{:key row}
     [view-swiper-slide i row])])

(screen/register!
 :screen/home
 (fn [i]
   (let [query-result (db/to-query-result i popular-media-query)
         rows (:query-result/rows query-result)]
     [:div.w-full.flex-1.flex.flex-col.overflow-hidden
      [top-bar/view {:top-bar/title "Home"}]
      [view-swiper i rows]
      (when (empty? rows)
        [image/view {:class "w-full h-full"}])
      [top-level-bottom-buttons/view i]])))
