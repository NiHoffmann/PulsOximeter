# Ergebnisse

## Funktionalität des Heart Beat Sensors
- Die Hardware des Heart Beat Sensors ist funktionsfähig.
- Alle gesetzten Ziele wurden erreicht.

## Verbesserungspotential
- Der USB-Port konnte nicht angebracht werden, daher wurde vollständig auf BLE (Bluetooth Low Energy) ausgewichen.
- Der Heart Beat Filter arbeitet zuverlässig, jedoch ist die Peak Detection noch nicht optimal.
- Die CO₂-Berechnung ist technisch korrekt implementiert, jedoch noch nicht kalibriert (siehe Kommentar im Quellcode).

## Anmerkungen zur Datenauswertung
- Im Standardfall würden die Rohdaten an eine App übertragen und dort ausgewertet. Dies ermöglicht eine genauere Berechnung der Peaks (z. B. mittels Python-Skript), die zwar rechenintensiv ist, aber sehr präzise Ergebnisse liefert.
- Für eine Produktionslösung könnte das Erfassen und Filtern der Daten auf dem Gerät erfolgen, während die finale Auswertung in der App stattfindet. Für dieses Embedded-Projekt wurde jedoch der Fokus auf die direkte Ausgabe auf dem Gerät gelegt.

## Disclaimer - Umsetzung und AI-unterstützte Komponenten
Alle Ideen, theoretischen Überlegungen und technischen Umsetzungen wurden eigenhändig durchgeführt, mit Ausnahme der folgenden Komponenten, bei denen AI-Tools zur Erstellung des Grundgerüsts genutzt wurden. Sämtliche Erweiterungen, Anpassungen und Implementierungen darüber hinaus erfolgten manuell:  

- **PeakFinder:** Refactoring von C++ `vector`-basierter Implementierung zu einer statischen Library.  
- **Plot_IR.py:** AI-generiertes Grundgerüst zur Datenvisualisierung; alle weiteren Funktionen und Anpassungen eigenhändig implementiert.  
- **BLE-test.py:** AI-generiertes Grundgerüst für den Datenaustausch; alle weiteren Funktionen und Anpassungen eigenhändig implementiert.  
- **Debugging:** Unterstützung bei der Fehlersuche und Behebung von Problemen.
- **Überarbeitung von Texten:** Formulierung, Rechtschreibung und Grammatik.