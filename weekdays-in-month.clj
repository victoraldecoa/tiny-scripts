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
  {:from from
   :days (->> (range from to)
              (remove skip)
              (map #(LocalDate/of year month %))
              (filter weekday?))})

(defn logged-work
  [{:keys [work-log]} {:keys [days from]}]
  (->> days
       (map formatted)
       (map (partial log-form (:en (work-log from))))
       (str/join "\n")))

(defn- final-total
  [{:keys [hourly-rate]}
   working-days-ranges]
  (let [days (mapcat :days working-days-ranges)]
    (* 7 hourly-rate (count days))))

(defn- grouped 
  [{:keys [work-log] :as args}
    working-days-ranges]
  (str 
   (->> working-days-ranges
        (map #(str (* 7 (count (:days %))) " horas - " (:pt (work-log (:from %)))))
        (str/join "\n"))
 		"\n\nValor final: USD " (final-total args working-days-ranges)))

(defn month-work
  [{:keys [year month work-log] :as args}]
  (let [last-day (.lengthOfMonth (YearMonth/of year month))
        ranges (partition 2 1 (conj (vec (keys work-log)) (inc last-day)))
        working-days-ranges (->> ranges
                                 (map #(working-days (merge args {:from (first %)
                                                                  :to (last %)}))))]
    (str
     (->> working-days-ranges
          (map (partial logged-work args))
          (str/join "\n"))
     "\nFinal Total / Balance Due: USD " (final-total args working-days-ranges)
   	 "\n\n" (grouped args working-days-ranges))))

(def cli-options {:month {:coerce :long}
                  :year {:default (.getYear (LocalDate/now)) :coerce :long}
                  :language {:default :en :coerce :keyword}
                  :hourly-rate {:coerce :long}
                  :file {:coerce :string}})

(defn print-report!
  []
  (let [{:keys [file] :as args} (cli/parse-opts *command-line-args* {:spec cli-options})
        content (read-string (slurp file))
        params (merge args content)]
    (println (month-work params))))

(print-report!)
