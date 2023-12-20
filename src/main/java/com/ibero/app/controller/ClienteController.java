package com.ibero.app.controller;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.HttpHeaders;
import com.ibero.app.models.entity.Cliente;
import com.ibero.app.models.service.IClienteService;
import com.ibero.app.models.service.IUploadFileService;
import com.ibero.app.util.paginator.PageRender;

import jakarta.validation.Valid;

@Controller
//@RequestMapping("/cliente")
@SessionAttributes("cliente")
public class ClienteController {
	
	@Autowired
	private IClienteService clienteService;
	
	@Autowired
	private IUploadFileService uploadFileService;
	
	@RequestMapping(value = "/uploads/{filename:.+}", method = RequestMethod.GET)
	public ResponseEntity<Resource> showPhoto(@PathVariable String filename) {//import ...core.io.Resource;
		Resource recurso= null;
		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		//headers->springframework.http.HttpHeaders;
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename()+"\"").body(recurso);
	}
	
	@RequestMapping(value = "/ver/{id}", method = RequestMethod.GET)
	public String ShowDetalleCliente(@PathVariable(value="id") Long id,Map<String,Object> model, RedirectAttributes flash) {
		Cliente cliente = clienteService.findOne(id);
		if(cliente == null) {
			flash.addFlashAttribute("error", "El cliente mp existe en la base de datos");
			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Detalle cliente : " + cliente.getNombre());
		return "ver";
	}
	
	@RequestMapping(value = "/listar", method = RequestMethod.GET)
	public String ListarCliente(@RequestParam(name = "page",defaultValue = "0")int page, Model model) {
		
		Pageable pageRequest =  PageRequest.of(page, 4);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		
		PageRender<Cliente> pageRender = new PageRender<Cliente>("/listar", clientes);
		
		model.addAttribute("titulo", "Listado de clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";	
	}
	
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public String showForm(Map<String, Object> model) {
		Cliente cliente = new Cliente();
		model.put("titulo", "Registro de Clientes");
		model.put("cliente",cliente );
		return "form";	
	}
	
	@RequestMapping(value = "/form/{id}", method = RequestMethod.GET)
	public String editarForm(@PathVariable(value = "id") Long id,Map<String, Object> model,RedirectAttributes flash) {
		Cliente cliente = null;
		if(id > 0) {
			cliente = clienteService.findOne(id);
			if(cliente == null) {
				flash.addFlashAttribute("error", "El ID del cliente no existe en la BBDD");
				return "redirect:/listar";
			}
		}else {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser 0");
			return "redirect:/listar";		
		}
		model.put("titulo", "Editar Cliente");
		model.put("cliente",cliente );
		return "form";	
	}
	
	@RequestMapping(value = "/form", method = RequestMethod.POST)
	public String processForm(@Valid Cliente cliente,BindingResult result,Model model,@RequestParam("file") MultipartFile foto,
			RedirectAttributes flash, SessionStatus status) {
		if(result.hasErrors()) {
			model.addAttribute("titulo", "Registro de Clientes");
			return "form";
		}
		//Procesar foto
		if(!foto.isEmpty()) {
		
			if(cliente.getId() != null 
					&& cliente.getId() > 0 
					&& cliente.getFoto() != null 
					&& cliente.getFoto().length() > 0) {
				uploadFileService.delete(cliente.getFoto());
			}
			String uniqueFile = null;
			try {
				uniqueFile = uploadFileService.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}
			flash.addFlashAttribute("info", "Has subido correctamente '" + uniqueFile + "'");
			cliente.setFoto(uniqueFile);
			
		}
		
		String mensajeFlash = (cliente.getId() != null)?"Cliente editado con éxito":"Cliente creado con éxito";
		clienteService.guardar(cliente);
		status.setComplete();
		flash.addFlashAttribute("success",mensajeFlash);
		return "redirect:listar";	
		}
	
	@RequestMapping(value = "/eliminar/{id}", method = RequestMethod.GET)
	public String eliminarCliente(@PathVariable(value = "id") Long id,RedirectAttributes flash) {
		if(id > 0) {
			Cliente cliente = clienteService.findOne(id);
			
			clienteService.delete(id);
			flash.addFlashAttribute("success", "Cliente eliminado con éxito");
			if(uploadFileService.delete(cliente.getFoto())) {
				flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con exito!");
			}			
		}
		return "redirect:/listar";
	}

}
