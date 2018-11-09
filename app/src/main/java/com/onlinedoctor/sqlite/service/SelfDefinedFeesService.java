package com.onlinedoctor.sqlite.service;

import java.util.List;

import com.onlinedoctor.pojo.mine.SelfDefinedFee;

public interface SelfDefinedFeesService {
	public long add(SelfDefinedFee sdf);
	public boolean update(SelfDefinedFee sdf);
	public boolean delete(SelfDefinedFee sdf);
	public List<SelfDefinedFee> getAll();
	public SelfDefinedFee queryGlobalId(long globalId);
	public SelfDefinedFee queryId(int id);
	public boolean clean();
}
