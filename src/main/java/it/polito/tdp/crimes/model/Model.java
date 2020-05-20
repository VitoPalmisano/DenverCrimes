package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private EventsDao dao;
	private Graph<String, DefaultWeightedEdge> grafo;
	
	private List<String> best;
	
	public Model() {
		dao = new EventsDao();
	}
	
	public List<Integer> getMesi(){
		return dao.getMesi();
	}
	
	public List<String> getCategorie(){
		return dao.getCategorie();
	}
	
	public void creaGrafo(String categoria, int mese) {
		grafo = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		List<Adiacenza> adiacenze = dao.getAdiacenze(categoria, mese);
		
		for(Adiacenza a : adiacenze) {
			if(!grafo.containsVertex(a.getV1()));
				grafo.addVertex(a.getV1());
			if(!grafo.containsVertex(a.getV2()));
				grafo.addVertex(a.getV2());
				
			if(grafo.getEdge(a.getV1(), a.getV2())==null){
				Graphs.addEdgeWithVertices(grafo, a.getV1(), a.getV2(), a.getPeso());
			}
		}
		System.out.println(String.format("Grafo creato con %d vertici e %d archi", grafo.vertexSet().size(), grafo.edgeSet().size()));
	}

	public List<Arco> getArchi(){
		double pesoMedio = 0.0;
		for(DefaultWeightedEdge e : grafo.edgeSet()) {
			pesoMedio += grafo.getEdgeWeight(e);
		}
		pesoMedio = pesoMedio/grafo.edgeSet().size();
		
		List<Arco> archi = new ArrayList<>();
		for(DefaultWeightedEdge e : grafo.edgeSet()) {
			if(grafo.getEdgeWeight(e)>pesoMedio) {
				archi.add(new Arco(grafo.getEdgeSource(e), grafo.getEdgeTarget(e), grafo.getEdgeWeight(e)));
			}
		}
		Collections.sort(archi);
		return archi;
	}
	
	public List<String> trovaPercorso(String sorgente, String destinazione){
		List<String> parziale = new ArrayList<String>();
		best = new ArrayList<String>();
		parziale.add(sorgente);
		trovaRicorsivo(destinazione, parziale, 0);
		return best;
	}

	private void trovaRicorsivo(String destinazione, List<String> parziale, int livello) {
		
		// Caso terminale: quando l'ultimo vertice inserito in parziale e' uguale alla destinazione
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			if(parziale.size() > best.size())
				this.best = new ArrayList<String>(parziale);
			return;
		}
		
		// Scorro i vicini dell'ultimmmo vertice inserito in parziale
		for(String vicino : Graphs.neighborListOf(grafo, parziale.get(parziale.size()-1))) {
			// Cammino aciclico -> controllo che il vertice non sia gia' in parziale
			if(!parziale.contains(vicino)) {
				// provo ad aggiungere
				parziale.add(vicino);
				// continuo la ricorsione
				this.trovaRicorsivo(destinazione, parziale, livello+1);
				// faccio backtracking
				parziale.remove(parziale.size()-1);
			}
		}
		
	}
}
