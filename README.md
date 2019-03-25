# adserver

Simple ad matching engine.

## Building

Download and install [Leiningen](https://leiningen.org/#install), then build uberjar:

    $ lein uberjar

This will produce executable jarfile of the service.

## Usage
To run you need java installed. To run the service:

    $ java -jar adserver-0.1.0-standalone.jar [args]

## Limitations

The service was built with following limitations in mind (either deferred to a separate layer or a service, or just to draw boudaries):

* No authorization is explicitly provided (but can be opt in for as a ring layer)
* Deserialization and serialization is expected to be realized before calling the ad service. Specifically the service will merely return a pointer to the actual ad content and expects to have deserialized 
* No caching.
* The service is by design symmetric so that bastion wrapper (like graphql) can select relevant entity fields.

## License

Copyright © 2019 kapware.com

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
