package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Lance;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.mockito.Mockito;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EncerradorDeLeilaoTest {
	
	private EncerradorDeLeilao encerrador;
	private RepositorioDeLeiloes mockLeilaoDao;

	@Before
	public void setUp(){
        mockLeilaoDao = Mockito.mock(LeilaoDao.class);
        encerrador = new EncerradorDeLeilao(mockLeilaoDao);
	}

    @Test
    public void naoFazNadaCasoNaoAjaLeilao() {
        List<Leilao> listaLeiloes = new ArrayList<Leilao>();

        Mockito.when(mockLeilaoDao.correntes()).thenReturn(listaLeiloes);

        encerrador.encerra();

        Assert.assertEquals(0, encerrador.getTotalEncerrados());
    }

    @Test
    public void naoDeveEncerrarLeiloesQueComecaramOntem() {
        Calendar antiga = Calendar.getInstance();
        Calendar ontem = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        ontem.set(2019, 9, 29);

        Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(ontem).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Play").naData(ontem).constroi();
        Leilao leilao3 = new CriadorDeLeilao().para("Xbox").naData(antiga).constroi();
        List<Leilao> listaLeiloes = Arrays.asList(leilao1, leilao2, leilao3);

        Mockito.when(mockLeilaoDao.correntes()).thenReturn(listaLeiloes);

        encerrador.encerra();

        Assert.assertEquals(1, encerrador.getTotalEncerrados());
        Assert.assertFalse(leilao1.isEncerrado());
        Assert.assertFalse(leilao2.isEncerrado());
        Assert.assertTrue(leilao3.isEncerrado());
    }

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Play").naData(antiga).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);

		Mockito.when(mockLeilaoDao.correntes()).thenReturn(leiloesAntigos);

		encerrador.encerra();

		Assert.assertEquals(2, encerrador.getTotalEncerrados());
		Assert.assertTrue(leilao1.isEncerrado());
		Assert.assertTrue(leilao2.isEncerrado());
	}

    @Test
    public void deveAtualizarLeiloesEncerrados() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();

        RepositorioDeLeiloes daoFalso = Mockito.mock(RepositorioDeLeiloes.class);
        Mockito.when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
        encerrador.encerra();

        Mockito.verify(daoFalso).atualiza(leilao1);
    }

    @Test
    public void naoDeveAtualizarNenhumLeilao() {
        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(ontem).constroi();

        RepositorioDeLeiloes daoFalso = Mockito.mock(RepositorioDeLeiloes.class);
        Mockito.when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
        encerrador.encerra();

        Mockito.verify(daoFalso, Mockito.never()).atualiza(leilao1);
    }
}
