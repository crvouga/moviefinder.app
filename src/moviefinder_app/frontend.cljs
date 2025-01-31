(ns moviefinder-app.frontend
  (:require [moviefinder-app.frontend.store :as store]
            [moviefinder-app.media.frontend]
            [moviefinder-app.home.frontend]
            [moviefinder-app.auth.frontend]
            [moviefinder-app.rpc.frontend]
            [moviefinder-app.profile.frontend]
            [moviefinder-app.frontend.db]
            [moviefinder-app.frontend.runtime]
            [moviefinder-app.frontend.toaster :as toaster]
            [moviefinder-app.frontend.screen :as screen]))


(defn view [i]
  [:div {:class "fixed left-1/2 top-1/2 flex h-[100dvh] w-screen -translate-x-1/2 -translate-y-1/2 items-center justify-center overflow-hidden bg-black text-white"}
   [:div {:class "relative flex h-full max-h-[915px] w-full max-w-[520px] flex-col items-center justify-center overflow-hidden rounded min-[520px]:border border-neutral-500"}
    (toaster/view i)
    (screen/view i)]])

(defn -main []
  (store/initialize! view))
