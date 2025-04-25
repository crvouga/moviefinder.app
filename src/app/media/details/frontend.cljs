(ns app.media.details.frontend
  (:require
   [app.frontend.screen :as screen]
   [core.ui.top-bar :as top-bar]
   [app.frontend.db :as db]
   [core.ui.image :as image]
   [clojure.core.async :refer [<! go-loop]]
   [core.program :as p]))

(go-loop []
  (let [_ (<! (p/take! ::clicked-back))]
    (p/put! [:screen/push [:screen/home]])
    (recur)))

(p/reg-reducer ::set-media-details
               (fn [state msg]
                 (assoc state ::media-details (second msg))))

(defn view-backdrop [media]
  [image/view
   {:class "w-full aspect-video object-cover"
    :image/url (:media/backdrop-url media)
    :image/alt (:media/title media)}])

(defn view-top-bar [media]
  [top-bar/view
   {:top-bar/on-back #(p/put! [::clicked-back])
    :top-bar/title (:media/title media)}])

(defn view-title [media]
  [:div.text-3xl.font-bold (:media/title media)])

(defn view-overview [media]
  [:div.text-lg.text-neutral-300 (:media/overview media)])

(screen/register
 :screen/media-details
 (fn [input]
   (let [state input
         payload (screen/screen-payload state)
         media (db/to-entity state (:media/id payload))]
     [:div.w-full.h-full.flex.flex-col
      [view-top-bar media]
      [view-backdrop media]
      [:div.w-full.text-center.flex.flex-col.items-center.justify-center.p-6.gap-3
       [view-title media]
       [view-overview media]]])))
