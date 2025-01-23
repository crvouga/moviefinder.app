(ns moviefinder-app.frontend.ui.top-bar)


(defn view [i]
  (let [title (-> i :top-bar/title)]
    [:nav.flex.items-center.justify-center.w-full.h-20
     [:p.font-bold.text-lg title]]))