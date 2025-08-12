setup:
	mkdir -p run/
	./gradlew build
reinit:
	rm -rf run/
	mkdir -p run/
	./gradlew build