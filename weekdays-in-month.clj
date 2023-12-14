#!/usr/bin/env bb

; example:
; ./weekdays-in-month.clj --month 12 --append ", worked 8 hours in a script for creating an invoice"

(require '[clojure.string :as str])
(require '[babashka.cli :as cli])
(import '[java.time DayOfWeek LocalDate YearMonth])
(import '[java.time.format DateTimeFormatter])

(defn day-of-week-int
  [^LocalDate date]
  (.getValue (.getDayOfWeek date)))

(defn weekday?
  [^LocalDate date]
  (<= (day-of-week-int date) (.getValue DayOfWeek/FRIDAY)))

(defn formatted
  [^LocalDate date]
  {:formatted (.format date (DateTimeFormatter/ofPattern "EEEE, MMMM d, yyyy"))
   :date date})

(defn log-form
  [log {:keys [formatted date]}]
  (str formatted ": 7 hours - " log (when (= (.getDayOfWeek date) DayOfWeek/FRIDAY) "\n")))

(defn working-days
  [{:keys [from to month year skip]}]
  (->> (range from to)
       (remove skip)
       (map #(LocalDate/of year month %))
       (filter weekday?)))

(defn logged-work
  [{:keys [log] :as args}]
  (->> (working-days args)
       (map formatted)
       (map (partial log-form log))
       (str/join "\n")))

(defn- final-total
  [{:keys [hourly-rate] :as args}]
  (* 7 hourly-rate (count (working-days args))))

(defn month-work
  [{:keys [year month work-log hourly-rate] :as args}]
  (let [last-day (.lengthOfMonth (YearMonth/of year month))
        ranges (partition 2 1 (conj (vec (keys work-log)) (inc last-day)))]
    (str
     (->> ranges
          (map #(logged-work (merge args {:from (first %)
                                          :to (last %)
                                          :log (work-log (first %))})))
          (str/join "\n"))
     "\nFinal Total / Balance Due: USD " (final-total (merge args {:from 1
                                                                   :to last-day})))))

(def cli-options {:month {:coerce :long}
                  :year {:default (.getYear (LocalDate/now)) :coerce :long}
                  :language {:default :en :coerce :keyword}
                  :hourly-rate {:coerce :long}
                  :file {:coerce :string}})

(defn print-report!
  []
  (let [{:keys [file] :as args} (cli/parse-opts *command-line-args* {:spec cli-options})
        content (read-string (slurp file))]
    (println (month-work (merge args content)))))

(print-report!)
