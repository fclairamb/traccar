TRACKER_SERVER=target/tracker-server.jar
SOURCES=$(shell find -name "*.java")
PACKAGE_FILES=$(shell find package)

all: $(TRACKER_SERVER)

$(TRACKER_SERVER): $(SOURCES) pom.xml
	mvn verify
	mvn dependency:copy-dependencies

install: $(TRACKER_SERVER)
	# Binaries
	mkdir -p $(DESTDIR)/usr/share/traccar/lib
	cp $(TRACKER_SERVER) $(DESTDIR)/usr/share/traccar
	cp target/dependency/* $(DESTDIR)/usr/share/traccar/lib

	mkdir -p $(DESTDIR)/usr/bin
	cp package/bin/* $(DESTDIR)/usr/bin
	cp tools/test.sh $(DESTDIR)/usr/bin/traccar_test_trackers
	chmod a+rx $(DESTDIR)/usr/bin/*

	# Config
	mkdir -p $(DESTDIR)/etc/traccar/
	cp setup/linux/traccar.cfg $(DESTDIR)/etc/traccar/traccar.cfg

	# Data dir
	mkdir -p $(DESTDIR)/var/lib/traccar

	# Logging
	mkdir -p $(DESTDIR)/var/log/traccar $(DESTDIR)/etc/logrotate.d
	cp package/logrotate.d/* $(DESTDIR)/etc/logrotate.d

	# Startup scripts
	mkdir -p $(DESTDIR)/etc/supervisor/conf.d
	cp package/supervisor.d/*.conf $(DESTDIR)/etc/supervisor/conf.d

package: $(TRACKER_SERVER) debian/*
	dpkg-buildpackage -b -us -uc
	mkdir -p dist/package
	mv ../*.deb dist/package/
	rm ../*.changes

deploy-local: package
	sudo dpkg -i dist/package/*.deb

deploy-remote:
	ls dist/package/*.deb || make package
	rsync -avz dist/package/*.deb $(TARGET):/tmp/traccar.deb
	ssh $(TARGET) dpkg -i /tmp/traccar.deb

clean:
	[ ! -d dist ] || rm -R dist

