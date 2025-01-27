(ns moviefinder-app.media.details.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.store :as store]
   [moviefinder-app.frontend.db :as db]))


(defn view-backdrop [i]
  [:img.w-full.aspect-video.object-cover
   {:src (:media/backdrop-url i)
    :alt (:media/title i)}])

(defn view-top-bar [i]
  [top-bar/view
   {:top-bar/on-back #(store/put! i [:screen/clicked-link [:screen/home]])
    :top-bar/title (:media/title i)}])

(defn view-title [i]
  [:div.text-3xl.font-bold (:media/title i)])

(defn view-overview [i]
  [:div.text-lg.text-neutral-300 (:media/overview i)])

(screen/register!
 :screen/media-details
 (fn [i]
   (let [payload (screen/screen-payload i)
         media (db/to-entity i (:media/id payload))
         i (merge i media)]
     [:div.w-full.h-full.flex.flex-col
      [view-top-bar i]
      [view-backdrop i]
      [:div.w-full.text-center.flex.flex-col.items-center.justify-center.p-6.gap-3
       [view-title i]
       [view-overview i]]])))
