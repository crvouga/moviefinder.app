(ns moviefinder-app.media.details.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.store :as store]
   [moviefinder-app.frontend.db :as db]))


(screen/register!
 :screen/media-details
 (fn [i]
   (let [payload (screen/screen-payload i)
         media (db/to-entity i (:media/id payload))]
     [:div.w-full.h-full.flex.flex-col
      [top-bar/view {:top-bar/on-back #(store/put! i [:screen/clicked-link [:screen/home]])
                     :top-bar/title (:media/title media)}]
      [:img.w-full.aspect-video.object-cover
       {:src (:media/backdrop-url media)
        :alt (:media/title media)}]
      [:div (pr-str media)]])))