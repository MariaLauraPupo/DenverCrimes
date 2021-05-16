package it.polito.tdp.crimes.model;

import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	private SimpleWeightedGraph<String,DefaultWeightedEdge> grafo;
	//Abbiamo dei vertici di tipo String => essendo un tipo semplice e non un oggetto 
	//=> non faremo delle new 
	//=> non abbiamo bisogno dell'idMap
	private EventsDao dao;
	private List<String> percorsoMigliore;
	
	public Model() {
		dao = new EventsDao();
	}
	
	public List<String> getCategorie(){
		return dao.getCategorie();
	}
	
	public void creaGrafo(String categoria, int mese) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiunta vertici
		Graphs.addAllVertices(grafo, dao.getVertici(categoria , mese));
		
		//aggiunta archi
		for(Adiacenza a : dao.getArchi(categoria, mese)) {
			//se non c'è ancora l'arco
			//questo serve nel caso in cui nella query invce del > abbia messo !=
			if(this.grafo.getEdge(a.getV1(), a.getV2()) == null){
				Graphs.addEdgeWithVertices(grafo, a.getV1(), a.getV2());
			}
		}
		System.out.println("vertici: "+ this.grafo.vertexSet().size());
		System.out.println("archi: "+ this.grafo.edgeSet().size());

		
	}
	public List<Adiacenza> getArchi(){
		//calcolo prima il preso medio degli archi presenti nel grafo
		double pesoMedio = 0.0;
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			pesoMedio += this.grafo.getEdgeWeight(e);//incremento il peso
		}
		pesoMedio = pesoMedio/this.grafo.edgeSet().size();//lo divido per il numero di archi
			
		//filtro gli archi tenendo solo quelli che hanno peso maggiore del paso medio
		
		List<Adiacenza> result = new LinkedList<>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {

			if(this.grafo.getEdgeWeight(e) > pesoMedio)
				result.add(new Adiacenza(this.grafo.getEdgeSource(e),this.grafo.getEdgeTarget(e),this.grafo.getEdgeWeight(e)));
		}
		return result;
	}
	
	public List<String> trovaPercorso(String sorgente, String destinazione){
		//voglio trovare il percorso più lungo
		//è un problema di ricerca dell'ottimo
		this.percorsoMigliore = new LinkedList<>();
		List<String> parziale = new LinkedList<>();
		parziale.add(sorgente);
		cerca(destinazione, parziale);
		return this.percorsoMigliore;
		
	}
	private void cerca(String destinazione, List<String> parziale) {
		//caso terminale
		//quando siamo arrivati a destinazione 
		//=> quando l'ultimo elemento di parziale coincide con la nostra destinazione
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			//devo controllare che questo percorso sia il migliore che ho visto fino ad adesso
			//cioè,in questo caso, devo avere il maggior numero di vertici visitati
			if(parziale.size() > this.percorsoMigliore.size() ) {
				this.percorsoMigliore = new LinkedList<> (parziale);
			}
			return;
		}
		
		//altrimenti devo aggiungere un nuovo vertice per proseguire un nuovo percorso
		//scorro i vicini dell'ultimo inserito e provo ad aggiungerli uno ad uno
		for(String vicino : Graphs.neighborListOf(grafo,parziale.get(parziale.size()-1))) {
			//per ogni vicino
			if(!parziale.contains(vicino)) {
				parziale.add(vicino);
				cerca(destinazione, parziale);
				parziale.remove(parziale.size()-1);
			}
		}
		
	}
}
