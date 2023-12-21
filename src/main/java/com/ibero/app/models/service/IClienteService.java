package com.ibero.app.models.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ibero.app.models.entity.Cliente;
import com.ibero.app.models.entity.Factura;
import com.ibero.app.models.entity.Producto;

public interface IClienteService {

	public List<Cliente> findAll();
	public Page<Cliente> findAll(Pageable pageable);
	public void guardar(Cliente cliente);
	public Cliente findOne(Long id);
	public void delete(Long id);
	
	public List<Producto> findByName(String term);
	
	public void guardarFactura(Factura factura);
	
	public Producto findProductoById(Long id);
	
	public Factura findFacturaById(Long id);
	
	public void deleteFactura(Long id);
	
}
