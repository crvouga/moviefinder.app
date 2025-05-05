(ns app.auth.logout.frontend
  (:require
   [app.frontend.mod :as mod]
   [clojure.core.async :as a]
   [lib.program :as p]
   [lib.result :as result]
   [lib.ui.confirmation :as confirmation]
   [app.frontend.toast :as toast]))



(defn- logic [i]
  (p/reg-reducer i ::set (fn [s [_ k v]] (assoc s k v)))
  (p/take-every! i :logout/logout (fn [] (p/put! i [::set ::opened ::confirmation])))
  (p/take-every! i ::clicked-cancel (fn [] (p/put! i [::set ::opened nil])))
  (p/take-every! i ::clicked-close (fn [] (p/put! i [::set ::opened nil])))
  (a/go-loop []
    (let [_ (a/<! (p/take! i ::clicked-confirmed))]
      (p/put! i [::set ::request result/loading])
      (p/put! i [::set ::opened ::confirmation])
      (let [res (a/<! (p/eff! i [:rpc/send! [:rpc/logout]]))]
        (p/put! i [::set ::request res])
        (p/put! i [:current-user/hard-load])
        (when (result/ok? res)
          (p/put! i [::set ::opened nil])
          (p/put! i [:toaster/show (toast/info "Logged out")]))
        (recur)))))


(defn view [i]
  [confirmation/view
   {:confirmation/open? (-> i ::opened (= ::confirmation))
    :confirmation/title "Logout"
    :confirmation/description "Are you sure you want to logout?"
    :confirmation/cancel-text "Cancel"
    :confirmation/confirm-text "Logout"
    :confirmation/confirm-loading? (-> i ::request result/loading?)
    :confirmation/on-close #(p/put! i [::clicked-close])
    :confirmation/on-cancel #(p/put! i [::clicked-cancel])
    :confirmation/on-confirm #(p/put! i [::clicked-confirmed])}])

(mod/reg {:mod/name ::mod
          :mod/logic logic
          :mod/view view})


