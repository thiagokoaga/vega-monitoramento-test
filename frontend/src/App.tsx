import { useEffect, useState } from 'react'
import { History, LayoutDashboard, Moon, PackageMinus, PackagePlus, RefreshCw, Sun, Warehouse } from 'lucide-react'
import { useTheme } from 'next-themes'
import { ApiError, listarIngredientes, listarVolume, type Ingrediente, type VolumePorTipo } from '@/api'
import { Button } from '@/components/ui/button'
import { Toaster } from '@/components/ui/sonner'
import { EntradaForm } from '@/dashboard/EntradaForm'
import { EstoqueTabela } from '@/dashboard/EstoqueTabela'
import { HistoricoPainel } from '@/dashboard/HistoricoPainel'
import { ResumoArmazem } from '@/dashboard/ResumoArmazem'
import { SaidaForm } from '@/dashboard/SaidaForm'
import { cn } from '@/lib/utils'

type Tela = 'dashboard' | 'entrada' | 'saida' | 'historico'

const TELAS: { id: Tela; nome: string; descricao: string; icone: typeof LayoutDashboard }[] = [
  {
    id: 'dashboard',
    nome: 'Dashboard',
    descricao: 'Totais por tipo, ocupação dos compartimentos e estoque.',
    icone: LayoutDashboard,
  },
  {
    id: 'entrada',
    nome: 'Nova entrada',
    descricao: 'Cadastra um ingrediente ou soma quantidade em um nome que já existe.',
    icone: PackagePlus,
  },
  {
    id: 'saida',
    nome: 'Saída',
    descricao: 'Retira quantidade do saldo e registra quem autorizou.',
    icone: PackageMinus,
  },
  {
    id: 'historico',
    nome: 'Histórico',
    descricao: 'Entradas, saídas e tentativas recusadas.',
    icone: History,
  },
]

export default function App() {
  const [tela, setTela] = useState<Tela>('dashboard')
  const [ingredientes, setIngredientes] = useState<Ingrediente[]>([])
  const [volume, setVolume] = useState<VolumePorTipo[]>([])
  const [saidaId, setSaidaId] = useState('')
  const [versao, setVersao] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const atual = TELAS.find((item) => item.id === tela) ?? TELAS[0]

  async function carregar() {
    try {
      const [lista, totais] = await Promise.all([listarIngredientes(), listarVolume()])
      setIngredientes(lista)
      setVolume(totais)
      setSaidaId((idAtual) => (lista.some((item) => String(item.id) === idAtual) ? idAtual : ''))
      setErro(null)
      setVersao((valor) => valor + 1)
    } catch (causa) {
      const message = causa instanceof ApiError ? causa.message : 'Não foi possível carregar o armazém.'
      setErro(message)
    } finally {
      setCarregando(false)
    }
  }

  useEffect(() => {
    void carregar()
  }, [])

  function usarNaSaida(id: string) {
    setSaidaId(id)
    setTela('saida')
  }

  return (
    <div className="flex min-h-svh bg-muted/40">
      <aside className="sticky top-0 flex h-svh w-16 shrink-0 flex-col border-r border-sidebar-border bg-sidebar text-sidebar-foreground sm:w-60">
        <div className="flex items-center gap-2 px-3 py-4">
          <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <Warehouse className="size-4" />
          </div>
          <div className="hidden min-w-0 sm:block">
            <p className="truncate text-sm font-medium">Armazém</p>
            <p className="truncate text-xs text-muted-foreground">Ingredientes</p>
          </div>
        </div>
        <nav className="flex flex-1 flex-col gap-1 px-2">
          {TELAS.map((item) => {
            const Icone = item.icone
            const ativa = tela === item.id
            return (
              <Button
                key={item.id}
                type="button"
                variant="ghost"
                aria-current={ativa ? 'page' : undefined}
                className={cn(
                  'h-9 w-full justify-start px-2.5',
                  ativa && 'bg-sidebar-accent text-sidebar-accent-foreground',
                )}
                onClick={() => setTela(item.id)}
              >
                <Icone />
                <span className="hidden sm:inline">{item.nome}</span>
                <span className="sr-only sm:hidden">{item.nome}</span>
              </Button>
            )
          })}
        </nav>
        <div className="p-2">
          <TemaToggle />
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <main className="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 sm:px-6">
          <header className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-2xl font-medium tracking-tight">{atual.nome}</h1>
              <p className="text-sm text-muted-foreground">{atual.descricao}</p>
            </div>
            <Button type="button" variant="outline" onClick={() => void carregar()} disabled={carregando}>
              <RefreshCw className={carregando ? 'animate-spin' : undefined} />
              <span className="hidden sm:inline">Atualizar</span>
            </Button>
          </header>

          {erro ? <p className="rounded-xl bg-destructive/10 px-4 py-3 text-sm text-destructive">{erro}</p> : null}

          {tela === 'dashboard' ? (
            <>
              <ResumoArmazem ingredientes={ingredientes} volume={volume} />
              <EstoqueTabela ingredientes={ingredientes} onUsarNaSaida={usarNaSaida} />
            </>
          ) : null}

          {tela === 'entrada' ? (
            <>
              <ResumoArmazem ingredientes={ingredientes} volume={volume} />
              <EntradaForm onSucesso={carregar} />
            </>
          ) : null}

          {tela === 'saida' ? (
            <>
              <ResumoArmazem ingredientes={ingredientes} volume={volume} />
              <SaidaForm
                ingredientes={ingredientes}
                ingredienteId={saidaId}
                onIngredienteId={setSaidaId}
                onSucesso={carregar}
              />
            </>
          ) : null}

          {tela === 'historico' ? <HistoricoPainel versao={versao} /> : null}
        </main>
      </div>
      <Toaster position="top-right" />
    </div>
  )
}

function TemaToggle() {
  const { theme, setTheme } = useTheme()
  const escuro = theme === 'dark'

  return (
    <Button
      type="button"
      variant="ghost"
      className="h-9 w-full justify-start px-2.5"
      onClick={() => setTheme(escuro ? 'light' : 'dark')}
    >
      {escuro ? <Sun /> : <Moon />}
      <span className="hidden sm:inline">{escuro ? 'Tema claro' : 'Tema escuro'}</span>
      <span className="sr-only sm:hidden">{escuro ? 'Tema claro' : 'Tema escuro'}</span>
    </Button>
  )
}
