(ns app.auth.login.frontend.send-code
  (:require
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.frontend.toast :as toast]
   [clojure.core.async :refer [<! go-loop]]
   [core.program :as p]
   [core.result :as result]
   [core.ui.button :as button]
   [core.ui.text-field :as text-field]
   [core.ui.top-bar :as top-bar]))

(defn- logic [i]
  (p/reg-reducer i ::set-phone-number (fn [state msg] (assoc state ::phone-number (second msg))))
  (p/reg-reducer i ::set-request (fn [state msg] (assoc state ::request (second msg))))

  (go-loop []
    (let [msg (<! (p/take! i ::inputted-phone-number))]
      (p/put! i [::set-phone-number (-> msg second)])
      (recur)))

  (go-loop []
    (<! (p/take! i ::user-submitted-form))

    (p/put! i [::set-request result/loading])

    (let [state (p/state! i)
          phone-number (-> state ::phone-number)
          res (<! (p/eff! i [:rpc/send! [:rpc/send-code {:user/phone-number phone-number}]]))]

      (p/put! i [::set-request res])

      (when (result/ok? res)
        (p/put! i [:screen/push [:screen/login-verify-code {:user/phone-number phone-number}]])
        (p/put! i [:toaster/show (toast/info "Code sent")]))

      (when (result/err? res)
        (p/put! i [:toaster/show (toast/error (-> res :error/message))]))

      (recur))))


(defn- loading? [i]
  (-> i ::request result/loading?))

(defn view-field-phone-number [i]
  [text-field/view
   {:text-field/label "Phone Number"
    :text-field/value (-> i ::phone-number)
    :text-field/required? true
    :text-field/type :text-field-type/number-pad
    :text-field/disabled? (loading? i)
    :text-field/on-change #(p/put! i [::inputted-phone-number %])}])

(defn view-submit-button [i]
  [button/view
   {:button/type :button-type/submit
    :button/full? true
    :button/loading? (loading? i)
    :button/label "Send Code"}])

(defn- view-top-bar [i]
  [top-bar/view {:top-bar/title "Login with SMS"
                 :top-bar/on-back #(p/put! i [:screen/clicked-link [:screen/profile]])}])

(defn view-form [i & children]
  (vec
   (concat [:form.flex.flex-col.w-full.gap-6.p-6
            {:on-submit #(do (.preventDefault %) (p/put! i [::user-submitted-form]))}]
           children)))


(defn view [i]
  [screen/view-screen i :screen/login
   [view-top-bar i]
   [view-form i
    [view-field-phone-number i]
    [:div.w-full]
    [view-submit-button i]]])

(mod/reg
 {:mod/name :mod/login
  :mod/view-fn view
  :mod/logic-fn logic})
