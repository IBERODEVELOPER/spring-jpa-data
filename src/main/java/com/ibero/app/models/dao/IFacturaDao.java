package com.ibero.app.models.dao;

import org.springframework.data.repository.CrudRepository;

import com.ibero.app.models.entity.Factura;

public interface IFacturaDao extends CrudRepository<Factura, Long>{

}
