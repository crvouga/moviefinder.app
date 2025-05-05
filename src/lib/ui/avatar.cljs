(ns lib.ui.avatar
  (:require
   [lib.ui.cn :refer [cn]]
   [lib.ui.icon :as icon]))

(def default-size 64)

(defn- to-style [i]
  {:width (-> i :avatar/size (or default-size))
   :height (-> i :avatar/size (or default-size))})

(defn- view-image [i]
  [:img {:alt (:avatar/alt i)
         :src (:avatar/src i)
         :style (to-style i)
         :class (cn "aspect-square rounded-full overflow-hidden"
                    (:avatar/class i))}])


(defn- view-icon [i]
  [:div {:style (to-style i)
         :class (cn "aspect-square rounded-full overflow-hidden bg-neutral-600 grid place-items-center text-neutral-300"
                    (:avatar/class i))}
   [icon/user {:class "w-full h-full" :style {:margin-top "25%"}}]])


(defn- view-loading [i]
  [:div
   {:style (to-style i)
    :class (cn "aspect-square rounded-full overflow-hidden bg-neutral-600 grid place-items-center text-neutral-300 animate-pulse"
               (:avatar/class i))}])

(defn- image? [i]
  (not-empty (:avatar/src i)))

(defn loading? [i]
  (-> i :avatar/loading? (or false)))

(defn view [i]
  (cond
    (loading? i) (view-loading i)
    (image? i) (view-image i)
    :else (view-icon i)))
