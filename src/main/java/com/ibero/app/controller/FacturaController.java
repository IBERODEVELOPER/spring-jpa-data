package com.ibero.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ibero.app.models.entity.Cliente;
import com.ibero.app.models.entity.Factura;
import com.ibero.app.models.entity.ItemFactura;
import com.ibero.app.models.entity.Producto;
import com.ibero.app.models.service.IClienteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/factura")
@SessionAttributes("factura")
public class FacturaController {
	
	@Autowired
	private IClienteService clienteService;
	
	@RequestMapping(value = "/ver/{id}", method = RequestMethod.GET)
	public String ShowDetalleFactura(@PathVariable(value="id") Long id,
			Map<String,Object> model,
			RedirectAttributes flash) {
		
		Factura factura = clienteService.findFacturaById(id);
		
		if(factura == null) {
			flash.addFlashAttribute("error", "¡La factura no existe en la base de datos!");
			return "redirect:/listar";
		}
		model.put("factura", factura);
		model.put("titulo", "Factura: ".concat(factura.getDescripcion()));
		return "factura/ver";
	}
	
	
	@RequestMapping(value = "/form/{clienteId}", method = RequestMethod.GET)
	public String crearFactura(@PathVariable(value = "clienteId") Long clienteId, 
			Map<String,Object> model,
			RedirectAttributes flash) {
		
		Cliente cliente = clienteService.findOne(clienteId);
		
		if(cliente == null) {
			flash.addFlashAttribute("error", "El cliente no existe en la base de datos");
			return "redirect:/cliente/listar";
		}
		
		Factura factura = new Factura();
		factura.setCliente(cliente);
		
		model.put("factura",factura );
		model.put("titulo","Crear factura" );
		return "factura/form";
	}
	
	@GetMapping(value="/cargar-productos/{term}", produces = {"application/json"})
	public @ResponseBody List<Producto> cargarProductos(@PathVariable String term){
		return clienteService.findByName(term);
	}
	
	@PostMapping("/form")
	public String guardarFactura(@Valid Factura factura,BindingResult result,
			Model model,
			@RequestParam(name ="item_id[]",required= false) Long[] itemId,
			@RequestParam(name = "cantidad[]", required=false)Integer[] cantidad,
			RedirectAttributes flash,
			SessionStatus status){
		if(result.hasErrors()) {
			model.addAttribute("titulo", "Crear Factura");
			return "factura/form";
		}
		
		if(itemId == null || itemId.length == 0) {
			model.addAttribute("titulo", "Crear Factura");
			model.addAttribute("error", "Error: La factura NO puede no tener líneas!");
			return "factura/form";
		}
		
		for(int i=0;i<itemId.length;i++) {
			Producto producto = clienteService.findProductoById(itemId[i]);
			
			ItemFactura linea = new ItemFactura();
			linea.setCantidad(cantidad[i]);
			linea.setProducto(producto);
			factura.addItemFactura(linea);
		}
		clienteService.guardarFactura(factura);
		status.setComplete();
		
		flash.addFlashAttribute("success","Factura creada con éxito!");
		return "redirect:/ver/" + factura.getCliente().getId();
	}

}
