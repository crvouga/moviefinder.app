(ns app.auth.login.frontend.verify-code
  (:require
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.frontend.toast :as toast]
   [clojure.core.async :as a]
   [lib.err :as err]
   [lib.program :as p]
   [lib.result :as result]
   [lib.ui.button :as button]
   [lib.ui.text-field :as text-field]
   [lib.ui.top-bar :as top-bar]))

(defn to-phone-number [i]
  (-> i screen/to-screen-payload :user/phone-number))

(defn- loading? [i]
  (-> i  ::request result/loading?))

(defmethod err/message :verify-sms-err/wrong-code []
  "Wrong code")

(defn- logic [i]
  (p/reg-reducer i ::set-code (fn [state [_ code]] (assoc state ::code code)))
  (p/reg-reducer i ::set-request (fn [state [_ request]] (assoc state ::request request)))

  (p/take-every! i ::user-inputted-code (fn [[_ code]] (p/put! i [::set-code code])))

  (a/go-loop []
    (a/<! (p/take! i ::user-submitted-form))

    (p/put! i [::set-request result/loading])

    (let [state (p/state! i)
          code (::code state)
          phone-number (to-phone-number state)
          req [:rpc/verify-code {:user/phone-number phone-number :verify-sms/code code}]
          res (a/<! (p/eff! i [:rpc/send! req]))]

      (p/put! i [::set-request res])

      (when (result/ok? res)
        (p/put! i [:toaster/show (toast/info "Logged in")])
        (p/put! i [:screen/push [:screen/profile]]))

      (when (result/err? res)
        (println "error" res)
        (p/put! i [:toaster/show (toast/error (err/message res))]))
      (recur))))

;; 
;; 
;; 
;; 

(defn- view-code [i]
  [text-field/view
   {:text-field/label "Code"
    :text-field/value (-> i ::code)
    :text-field/type :text-field-type/number-pad
    :text-field/required? true
    :text-field/disabled? (loading? i)
    :text-field/on-change #(p/put! i [::user-inputted-code %])}])

(defn- view-submit [i]
  [button/view
   {:button/type :button-type/submit
    :button/loading? (loading? i)
    :button/full? true
    :button/label "Verify Code"}])

(defn- view-top-bar [i]
  [top-bar/view {:top-bar/title "Verify Code"
                 :top-bar/on-back #(p/put! i [:screen/clicked-link [:screen/login]])}])

(defn view-message [i]
  [:p.text-lg "Enter the code we sent to " [:span.font-bold (-> i screen/to-screen-payload :user/phone-number)]])

(defn view-form [i & children]
  (vec
   (concat [:form.flex.flex-col.w-full.gap-6.p-6
            {:on-submit #(do (.preventDefault %) (p/put! i [::user-submitted-form]))}]
           children)))

(defn view [i]
  [screen/view-screen i :screen/login-verify-code
   [view-top-bar i]
   [view-form i
    [view-message i]
    [view-code i]
    [:div.w-full]
    [view-submit i]]])

(mod/reg
 {:mod/name :mod/login-verify-code
  :mod/view-fn view
  :mod/logic-fn logic})