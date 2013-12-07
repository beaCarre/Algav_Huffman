
type role =
  | DRAWER
  | GUESSER 
      
type etat =
  | WAITING
  | PLAYING
  | EXITED

type client = {
  chan : Unix.file_descr;
  name : string ;
  mutable role : role ;
  mutable etat : etat;
  mutable score : int;
}

let clients = ref []

let clients_mutex = Mutex.create()

let serv_addr = ref (Unix.gethostbyname(Unix.gethostname())).Unix.h_addr_list.(0)

let get_option option defaut =
  let res = ref defaut in
  for i=1 to (Array.length Sys.argv)-1 do
      if  (Sys.argv.(i) = option) && (i < (Array.length Sys.argv))
      then
	res := Sys.argv.(i+1)
  done;
    !res

let timeout = int_of_string (get_option "-timeout" "30")
let max = int_of_string (get_option "-max" "5")
let dico = get_option "-dico" "dico.txt"
let port = int_of_string (get_option "-port" "2013")

let client_from_fd sd =
  List.find (fun c -> c.chan = sd ) !clients



let my_input_line fd =
  let s = " " and r = ref ""
  in while (ThreadUnix.read fd s 0 1 > 0) && s.[0] <> '\n' do r := !r ^s done ;
    print_string !r;
    !r
    
let my_output_line fd str =
  ignore (ThreadUnix.write fd str 0 (String.length str))
  (*if !trace_flag then Printf.printf "TRACE : command send : %s\n%!" str*)


let stop_thread_client sd =
  print_string "blabla";
  clients := List.filter (fun c -> c.chan <> sd) !clients;
  Unix.close sd;
  List.iter (fun c -> my_output_line c.chan (Printf.sprintf "EXITED/%s/\n%!" (client_from_fd sd).name) ) !clients;
  Thread.exit ()


let init_mot () =
  "mot"
  
let timer_word_find () =
  ()

let timer_round () =
  ()


let start_round namedrawer =
  let mot = init_mot () in
    List.iter (fun c -> c.role <- ( if c.name=namedrawer then DRAWER else GUESSER)) !clients;
    List.iter 
      (fun c -> my_output_line c.chan ( if c.name = namedrawer
					then (Printf.sprintf "NEW_ROUND/%s/%s/\n" c.name mot)
					else (Printf.sprintf "NEW_ROUND/%s/\n" c.name ))
      )
      !clients;
    ignore (Thread.create timer_round () )
      

let start_game () =
  List.iter (fun c -> c.etat <- PLAYING) !clients;
  List.iter (fun c -> start_round c.name) !clients

let split_commande cmd =
  Str.split (Str.regexp "/") cmd 
    


let th_joueur sd =
  let client = client_from_fd sd in
    while true do
      let commande_recue = my_input_line client.chan in
      let commande = split_commande commande_recue in
	match client.role , commande with
	  | _,"EXIT"::_ -> stop_thread_client sd
	  | r,cmd -> (* print_string (if r=GUESSER then "guesser\n%!" else "drawer\n%!");*)
	      List.iter (fun cm -> print_string cm) cmd  
    done
	      
let th_spectator sd =
  ()

let treat_connexion user sd =
  Mutex.lock clients_mutex;
  clients:={name=user;chan=sd;role=GUESSER;etat=WAITING;score=0}::!clients;
  my_output_line sd (Printf.sprintf "CONNECTED/%s/\n%!" user);
  if List.length (List.filter (fun c -> c.etat = WAITING) !clients) = 3 (*max*)
  then
    start_game ()(* clients ? *)
  else () (* attente des autres*) ;
  Mutex.unlock clients_mutex
	  

let rename name =
    match List.length ( List.filter ( fun c -> c.name = name ) !clients) with
	0 -> name
      | n -> name^"("^string_of_int n^")"



let connexion_client sd sa =
  let recue = my_input_line sd in
  let commande = split_commande recue in
    match commande with
      |"CONNECT"::name::_
	-> let new_name = rename name in
	  treat_connexion new_name sd ; 1
      (*|"LOGIN"::login::password::_
	-> if check_login login password = 1
	then 
	  begin 
	treat_connexion login sd;
	1
	end
	else  (* erreur log *) 0
	  
      |"REGISTER"::login::password::_ ->
	 if check_register login password = 1
	 then 
	   begin 
	     treat_connexion login sd;
	     1
	   end
	 else  (* erreur log *) 0
      |"SPECTATOR"::_ -> ()
      *)
      | _ -> 0
		   
let connexion sd sa =
  match connexion_client sd sa with
      0 -> stop_thread_client sd
    | 1 -> th_joueur sd 
    | 2 -> (* spectator *) ()
    | _ -> stop_thread_client sd
	

    
let serv_socket_run port =
  let sock = ThreadUnix.socket Unix.PF_INET Unix.SOCK_STREAM 0 in
  let mon_adresse = 
    (* (Unix.gethostbyname(Unix.gethostname()).Unix.h_addr_list.(0) in *)		    
    Unix.inet_addr_of_string "127.0.0.1" in

    Unix.bind sock (Unix.ADDR_INET(mon_adresse,port));
    Unix.listen sock 3;
    while true do
      let sd, sa = ThreadUnix.accept sock in
	begin
	  ignore (Thread.create 
		    (fun x -> (connexion sd sa) ; print_string "blop"; (stop_thread_client sd) )
		    () 
		 );
	  Printf.printf "fermeture client"
	end
    done
     
let () =
  Unix.handle_unix_error serv_socket_run port
