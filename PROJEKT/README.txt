DOKUMENTACJA PROJEKTU - KAROL PILARSKI s22682 23c

--ORGANIZACJA SIECI ORAZ KOUNIKACJI
  Pierwszy zainicjowany w�ze� zostaje korzeniem, kt�ry domy�lnie 
  koordunuje prac� sieci.Przechowuje adresy wszystkich innych w�z��w
  oraz zarz�dza sposobem alokacji danych.Komunikacja zaw�rwno z klientem,
  jak wewn�trz sieci odbywa si� w ca�o�ci za pomoc� protoko�u TCP. Serwery
  rozpoznaj� wiadomo�ci wewn�trzne po tym, �e otrzymana linia zaczyna si� 
  od s�owa "node", a nast�pnie zawiera komend� oraz potrzebne dane.


--KLASY
  1.NetworkNode - Podstawowa klasa serwera. Jej zadaniem jest wst�pna
  weryfikacja poprawno�ci podanych parametr�w, odczytanie ich oraz 
  inicjacja odpowiedniego obiektu w�z�a

  2.RootNode - klasa w�z�a g��wnego

  3.BranchNode - klasa wez��w-ga��zi, czyli pozosta�ych w�z��w

  4.NodeInfo - klasa u��twiaj�ca przechowywanie najwa�niejszych informacji 
  o w�z�ach

  5.AllocatedValue- klasa s�u��ca do przechowywania informacji o
  zaalokowanych zasobach; typ, ilo�� oraz w�a�ciciel.


--KOMUNIKACJA MI�DZY W�Z�AMI
  1.node new <port_tcp> <adres_ip> <lista_zasob�w> - dodanie
  nowego serwera do sieci
  2.node addRoot <tcp_port> - przesy�ane przez root do korzeni,
  s�u�y do ustawienia w serwerze adresu do serwera g��wnego
  3.node allocate <typ_zasobu> <ilo��> <id_klienta> - przesy�any 
  przez root do korzeni, przekazuje informacj� ile, jakiego i dla 
  kogo zasou zarezerwowa�
  4.node terminate - zako�czenie pracy w�z�a


--DODAWANIE NOWYCH W�Z��W DO SIECI
  1.Wywo�anie obiektu NetworkNode z ��danymi parametrami. Nast�pnie s� one 
  interpretowane i wykorzystane do wywo�ania klasy BranchNode.
  2.Klasa BranchNode wysy�a polecenie 
  "node new <port_tcp> <adres_ip> <lista_zasob�w>" do swojej
  bramy podanej w parametrze za pomoc� protoko�u TCP.
  3.Nowy serwer rozpoczyna oczekiwanie na po��czenie TCP.
  4.Kiedy brama otrzyma polecenie, przekazuje je w niezmienionej formie 
  do serwera g�ownego. Awaryjnie, je�li nie zna adresu korzenia, przesy�a
  polecenie do swojej bramy.
  5.Serwer g��wny po otrzymaniu polecenia zapami�tuje adres, port oraz dost�pne
  zasoby nowego serwera. Dodaje nowe zasoby do spisu wszytskich dost�pnych zasob�w.
  6.Serwer g��wny ��czy si� z now� ga��zi� protoko�em tcp, przesy�a polecenie 
  "node addRoot" z parametrami.
  7.Nowy serwer z otrzymanych danych zapami�tuje adres oraz port serwera g��wnego
  jako zmienn� rootAdress, aby m�c si� z nim bezpo�rednio komunikowa� 


--REZERWOWANIE ZASOB�W (je�li klient ��czy si� z g��wnym serwerem pomin�� kroki 1 i 2)
  1.Klient ��czy si� z dowolnym w�z�em sieci i wysy�a zapytanie w formie
  <id_klienta> <lista_zasob�w> 
  2.W�ze� po rozpoznaniu �e jest to zapytanie klienta(brak "node" na pocz�tku)
  otwiera dodatkowe po��czenie TCP, tym razem z serwerem g��wnym. Przesy�a mu
  zapytanie klienta i od teraz s�u�y jako po�rednik mi�dzy klientem a rootem.
  czeka na dane wys�ane przez g��wny serwer i od razu wysy�a je do klienta.
  3.Serwer g��wny sprawdza czy w sieci znajduje si� tyle zasob�w ile ��da klient za
  pomoc� tabeli pomocniczej kt�ra przechowuje dane o zsumowanej zawarto�ci zasob�w sieci.
  Je�li tak, wysy�a "ALLOCATED", je�li nie "FAILED".
  4.Serwer alfabetycznie pr�buje zaadresowa� ka�dy typ zasobu, pocz�tkowo na sobie, 
  nast�pnie, je�li jest taka potrzeba, sprawdza ka�dy w�ze� sieci po kolei szukaj�c
  odpowiedniego miejsca, nast�pnie alokuj�c tam dane. Je�li si� to uda, wysy�a kolejne
  linie <zas�b>:<liczno��>:<ip w�z�a>:<port w�z�a> do klienta.

--PRZYPADEK ROZPATRZONY ZA SUGESTI� PROWADZ�CEGO:
  Je�li klient wy�l� zapytanie do w�z�a kt�ry jeszcze nie zd��y� w pe�ni do��czy�
  si� do sieci, w�ze� u�yje swojej warto�ci gateway jak adresu g��wnego w�z�a.
  W ten spos�b to ten w�ze� skomunikuje si� z g��wnym w�z�em, a pierwszy przyjmie 
  rol� po�rednika mi�dzy nim a klientem.


--KO�CZENIE PRACY SIECI
  1.Kiedy w�ze� otrzyma polecenie "TERMINATE" od klienta przekazuje go do w�z�a
  g��wnego
  2.W�ze� g��wny rozsy�a polecenie zako�czenia pracy do ka�dego w�z�a
  3.W�z�y po otrzymaniu go wy��czaj� si�
  4.Na koniec wy��cza si� w�ze� g��wny


--KOMUNIKATY WY�WIETLANE NA KONSOLI
  1.Server listens on port: <port> - Pierwsze co pojawia si� na konsoli. M�wi na kt�rym
  porcie nas�uchuje dany serwer.
  2.Adding node <adres>:<port> - wy�wietlane przez root, podaje dane nowych w�z��w
  3.Forwarding node <adres1>:<port1> to <adres2>:<port2> - wy�wietlane przez ga��zie kt�re 
  pe�ni� funkcj� bramy. Informuje o przekazaniu danych nowego wez�a o adresie 
  <adres1>:<port1> do serwera g��wnego(lub bramy) o adresie <adres2>:<port2>.
  4.Root: <adres>:<port> - informacja o adresie serwera g��wnego pojawiaj�ca si� w konsoli
  ga��zi, zaraz po otrzymaniu tej informacji od sieci.
  5.Processing client: <client_id> - wy�wietlane przez serwer g��wny, informuje o rozpocz�ciu 
  obs�ugi danego klienta
  6.Allocating <ilo��> pieces of <typ_zasobu> for client <id_klienta> - Wy�wietlane podczas
  alokacji na ka�dym w�le serwera bior�cym udzia� w alokacji. Informuje o ilo�ci, typie
  i w�a�cicielu alokowanych danych.
  7.Terminating... - zamykanie sieci w�z��w

--KOMPILACJA
  1.W konsoli przejd� do folderu zawieraj�ce pliki projektu z rozszerzeniem .java
  2.U�yj komendy javac NodeNetwork.java
  3.Powinny zosta� wygenerowane pliki .class dla wszytskich potrzebnych klas

--URUCHAMIANIE
  1.W konsoli przejd� do folderu zawieraj�ce pliki projektu z rozszerzeniem .class
  2.U�yj komendy java NodeNetwork -ident <id_w�z�a> -tcpport <port_tcp> [-gateway <adres_bramy>:<port_bramy>] <lista_zasob�w>
  3.Dla pierwszego w�z�a nie podawaj parametru -gateway