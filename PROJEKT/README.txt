DOKUMENTACJA PROJEKTU - KAROL PILARSKI s22682 23c

--ORGANIZACJA SIECI ORAZ KOUNIKACJI
  Pierwszy zainicjowany wêze³ zostaje korzeniem, który domyœlnie 
  koordunuje pracê sieci.Przechowuje adresy wszystkich innych wêz³ów
  oraz zarz¹dza sposobem alokacji danych.Komunikacja zawórwno z klientem,
  jak wewn¹trz sieci odbywa siê w ca³oœci za pomoc¹ protoko³u TCP. Serwery
  rozpoznaj¹ wiadomoœci wewnêtrzne po tym, ¿e otrzymana linia zaczyna siê 
  od s³owa "node", a nastêpnie zawiera komendê oraz potrzebne dane.


--KLASY
  1.NetworkNode - Podstawowa klasa serwera. Jej zadaniem jest wstêpna
  weryfikacja poprawnoœci podanych parametrów, odczytanie ich oraz 
  inicjacja odpowiedniego obiektu wêz³a

  2.RootNode - klasa wêz³a g³ównego

  3.BranchNode - klasa wez³ów-ga³êzi, czyli pozosta³ych wêz³ów

  4.NodeInfo - klasa u³¹twiaj¹ca przechowywanie najwa¿niejszych informacji 
  o wêz³ach

  5.AllocatedValue- klasa s³u¿¹ca do przechowywania informacji o
  zaalokowanych zasobach; typ, iloœæ oraz w³aœciciel.


--KOMUNIKACJA MIÊDZY WÊZ£AMI
  1.node new <port_tcp> <adres_ip> <lista_zasobów> - dodanie
  nowego serwera do sieci
  2.node addRoot <tcp_port> - przesy³ane przez root do korzeni,
  s³u¿y do ustawienia w serwerze adresu do serwera g³ównego
  3.node allocate <typ_zasobu> <iloœæ> <id_klienta> - przesy³any 
  przez root do korzeni, przekazuje informacjê ile, jakiego i dla 
  kogo zasou zarezerwowaæ
  4.node terminate - zakoñczenie pracy wêz³a


--DODAWANIE NOWYCH WÊZ£ÓW DO SIECI
  1.Wywo³anie obiektu NetworkNode z ¿¹danymi parametrami. Nastêpnie s¹ one 
  interpretowane i wykorzystane do wywo³ania klasy BranchNode.
  2.Klasa BranchNode wysy³a polecenie 
  "node new <port_tcp> <adres_ip> <lista_zasobów>" do swojej
  bramy podanej w parametrze za pomoc¹ protoko³u TCP.
  3.Nowy serwer rozpoczyna oczekiwanie na po³¹czenie TCP.
  4.Kiedy brama otrzyma polecenie, przekazuje je w niezmienionej formie 
  do serwera g³ownego. Awaryjnie, jeœli nie zna adresu korzenia, przesy³a
  polecenie do swojej bramy.
  5.Serwer g³ówny po otrzymaniu polecenia zapamiêtuje adres, port oraz dostêpne
  zasoby nowego serwera. Dodaje nowe zasoby do spisu wszytskich dostêpnych zasobów.
  6.Serwer g³ówny ³¹czy siê z now¹ ga³êzi¹ protoko³em tcp, przesy³a polecenie 
  "node addRoot" z parametrami.
  7.Nowy serwer z otrzymanych danych zapamiêtuje adres oraz port serwera g³ównego
  jako zmienn¹ rootAdress, aby móc siê z nim bezpoœrednio komunikowaæ 


--REZERWOWANIE ZASOBÓW (jeœli klient ³¹czy siê z g³ównym serwerem pomin¹æ kroki 1 i 2)
  1.Klient ³¹czy siê z dowolnym wêz³em sieci i wysy³a zapytanie w formie
  <id_klienta> <lista_zasobów> 
  2.Wêze³ po rozpoznaniu ¿e jest to zapytanie klienta(brak "node" na pocz¹tku)
  otwiera dodatkowe po³¹czenie TCP, tym razem z serwerem g³ównym. Przesy³a mu
  zapytanie klienta i od teraz s³u¿y jako poœrednik miêdzy klientem a rootem.
  czeka na dane wys³ane przez g³ówny serwer i od razu wysy³a je do klienta.
  3.Serwer g³ówny sprawdza czy w sieci znajduje siê tyle zasobów ile ¿¹da klient za
  pomoc¹ tabeli pomocniczej która przechowuje dane o zsumowanej zawartoœci zasobów sieci.
  Jeœli tak, wysy³a "ALLOCATED", jeœli nie "FAILED".
  4.Serwer alfabetycznie próbuje zaadresowaæ ka¿dy typ zasobu, pocz¹tkowo na sobie, 
  nastêpnie, jeœli jest taka potrzeba, sprawdza ka¿dy wêze³ sieci po kolei szukaj¹c
  odpowiedniego miejsca, nastêpnie alokuj¹c tam dane. Jeœli siê to uda, wysy³a kolejne
  linie <zasób>:<licznoœæ>:<ip wêz³a>:<port wêz³a> do klienta.

--PRZYPADEK ROZPATRZONY ZA SUGESTI¥ PROWADZ¥CEGO:
  Jeœli klient wyœlê zapytanie do wêz³a który jeszcze nie zd¹¿y³ w pe³ni do³¹czyæ
  siê do sieci, wêze³ u¿yje swojej wartoœci gateway jak adresu g³ównego wêz³a.
  W ten sposób to ten wêze³ skomunikuje siê z g³ównym wêz³em, a pierwszy przyjmie 
  rolê poœrednika miêdzy nim a klientem.


--KOÑCZENIE PRACY SIECI
  1.Kiedy wêze³ otrzyma polecenie "TERMINATE" od klienta przekazuje go do wêz³a
  g³ównego
  2.Wêze³ g³ówny rozsy³a polecenie zakoñczenia pracy do ka¿dego wêz³a
  3.Wêz³y po otrzymaniu go wy³¹czaj¹ siê
  4.Na koniec wy³¹cza siê wêze³ g³ówny


--KOMUNIKATY WYŒWIETLANE NA KONSOLI
  1.Server listens on port: <port> - Pierwsze co pojawia siê na konsoli. Mówi na którym
  porcie nas³uchuje dany serwer.
  2.Adding node <adres>:<port> - wyœwietlane przez root, podaje dane nowych wêz³ów
  3.Forwarding node <adres1>:<port1> to <adres2>:<port2> - wyœwietlane przez ga³êzie które 
  pe³ni¹ funkcjê bramy. Informuje o przekazaniu danych nowego wez³a o adresie 
  <adres1>:<port1> do serwera g³ównego(lub bramy) o adresie <adres2>:<port2>.
  4.Root: <adres>:<port> - informacja o adresie serwera g³ównego pojawiaj¹ca siê w konsoli
  ga³êzi, zaraz po otrzymaniu tej informacji od sieci.
  5.Processing client: <client_id> - wyœwietlane przez serwer g³ówny, informuje o rozpoczêciu 
  obs³ugi danego klienta
  6.Allocating <iloœæ> pieces of <typ_zasobu> for client <id_klienta> - Wyœwietlane podczas
  alokacji na ka¿dym wêŸle serwera bior¹cym udzia³ w alokacji. Informuje o iloœci, typie
  i w³aœcicielu alokowanych danych.
  7.Terminating... - zamykanie sieci wêz³ów

--KOMPILACJA
  1.W konsoli przejdŸ do folderu zawieraj¹ce pliki projektu z rozszerzeniem .java
  2.U¿yj komendy javac NodeNetwork.java
  3.Powinny zostaæ wygenerowane pliki .class dla wszytskich potrzebnych klas

--URUCHAMIANIE
  1.W konsoli przejdŸ do folderu zawieraj¹ce pliki projektu z rozszerzeniem .class
  2.U¿yj komendy java NodeNetwork -ident <id_wêz³a> -tcpport <port_tcp> [-gateway <adres_bramy>:<port_bramy>] <lista_zasobów>
  3.Dla pierwszego wêz³a nie podawaj parametru -gateway